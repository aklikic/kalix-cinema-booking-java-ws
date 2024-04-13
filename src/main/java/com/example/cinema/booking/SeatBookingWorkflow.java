package com.example.cinema.booking;


import com.example.cinema.show.ShowCommand;
import com.example.cinema.show.ShowCommandError;
import com.example.cinema.show.ShowCommandResponse;
import com.example.cinema.show.ShowEntity;
import com.example.cinema.wallet.WalletCommand;
import com.example.cinema.wallet.WalletCommandError;
import com.example.cinema.wallet.WalletCommandResponse;
import com.example.cinema.wallet.WalletEntity;
import com.google.protobuf.any.Any;
import kalix.javasdk.DeferredCall;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.client.ComponentClient;
import kalix.javasdk.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;
import java.util.UUID;

import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static java.nio.charset.StandardCharsets.UTF_8;

@Id("id")
@TypeId("seat-reservation")
@RequestMapping("/seat-reservation/{id}")
public class SeatBookingWorkflow extends Workflow<SeatBookingState> {

  public static final String RESERVE_SEAT_STEP = "reserve-seat";
  public static final String CHARGE_WALLET_STEP = "charge-wallet";
  public static final String CANCEL_RESERVATION_STEP = "cancel-reservation";
  public static final String CONFIRM_RESERVATION_STEP = "confirm-reservation";
  public static final String REFUND_STEP = "refund";
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ComponentClient componentClient;

  public SeatBookingWorkflow(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  private String reservationId() {
    return commandContext().workflowId();
  }

  @PostMapping
  public Effect<SeatBookingCommandResponse> start(@RequestBody SeatBookingCommand bookSeat) {
    logger.info("Start seat booking workflow");
    if (currentState() != null) {
      return effects().error("seat reservation already exists", INVALID_ARGUMENT);
    } else {
      var updatedState = SeatBookingState.of(reservationId(), bookSeat.showId(), bookSeat.seatNumber(), bookSeat.walletId());
      return effects()
              .updateState(updatedState)
              .transitionTo(RESERVE_SEAT_STEP)
              .thenReply(SeatBookingCommandResponse.ok(updatedState));
    }
  }

  @GetMapping()
  public Effect<SeatBookingCommandResponse> getState() {
    if (currentState() == null) {
      return effects().reply(SeatBookingCommandResponse.error(SeatBookingCommandError.BOOKING_NOT_FOUND));
    } else {
      return effects().reply(SeatBookingCommandResponse.ok(currentState()));
    }
  }

  @Override
  public WorkflowDef<SeatBookingState> definition() {
    var reserveSeat = step(RESERVE_SEAT_STEP)
      .call(this::reserveSeat)
      .andThen(ShowCommandResponse.ShowReserveCommandResponse.class, this::chargeWalletOrStop);

    var chargeWallet = step(CHARGE_WALLET_STEP)
      .call(this::chargeWallet)
      .andThen(WalletCommandResponse.Ack.class, this::confirmOrCancelReservation);

    var confirmReservation = step(CONFIRM_RESERVATION_STEP)
      .call(this::confirmReservation)
      .andThen(ShowCommandResponse.Ack.class, this::endAsCompleted);

    var cancelReservation = step(CANCEL_RESERVATION_STEP)
      .call(this::cancelReservation)
      .andThen(ShowCommandResponse.Ack.class, this::endAsFailed);

    var refund = step(REFUND_STEP)
      .call(this::refund)
      .andThen(WalletCommandResponse.Ack.class, this::cancelReservation);

    return workflow()
      .defaultStepTimeout(Duration.ofSeconds(3))
      .addStep(reserveSeat, maxRetries(3).failoverTo(CANCEL_RESERVATION_STEP))
      .addStep(chargeWallet, maxRetries(3).failoverTo(REFUND_STEP))
      .addStep(confirmReservation)
      .addStep(cancelReservation)
      .addStep(refund);
  }

  private DeferredCall<Any, ShowCommandResponse.ShowReserveCommandResponse> reserveSeat() {
    logger.info("reserving seat");
    return componentClient.forEventSourcedEntity(currentState().showId())
            .call(ShowEntity::reserveSeat)
            .params(new ShowCommand.ReserveSeat(currentState().walletId(), currentState().reservationId(), currentState().seatNumber()));
  }

  private Effect.TransitionalEffect<Void> chargeWalletOrStop(ShowCommandResponse.ShowReserveCommandResponse response) {
    if(response.error() == ShowCommandError.NO_ERROR){
      return effects().updateState(currentState().asSeatReserved(response.price()))
              .transitionTo(CHARGE_WALLET_STEP);
    }else{
      return effects().updateState(currentState().asSeatBookingStateFailed(response.error().name())).end();
    }
  }

  private DeferredCall<Any, WalletCommandResponse.Ack> chargeWallet() {
    logger.info("charging wallet");
    var expenseId = currentState().reservationId();
    return componentClient.forEventSourcedEntity(currentState().walletId())
            .call(WalletEntity::chargeWallet)
            .params(new WalletCommand.ChargeWallet(currentState().price().get(), expenseId));
  }

  private Effect.TransitionalEffect<Void> confirmOrCancelReservation(WalletCommandResponse.Ack response) {
    if(response.error() == WalletCommandError.NO_ERROR){
      return effects()
              .updateState(currentState().asWalletCharged())
              .transitionTo(CONFIRM_RESERVATION_STEP);
    }else{
      //Here we know that wallet was not charged. We can just cancel reservation as compensation action
      logger.warn("charging wallet failed with: {}" , response.error());
      return effects()
              .updateState(currentState().asWalletChargeRejected(response.error().name()))
              .transitionTo(CANCEL_RESERVATION_STEP);
    }
  }


  private DeferredCall<Any, ShowCommandResponse.Ack> confirmReservation() {
    logger.info("confirming reservation");
    return componentClient.forEventSourcedEntity(currentState().showId())
            .call(ShowEntity::confirmSeatReservationPayment)
            .params(currentState().reservationId());
  }

  private Effect.TransitionalEffect<Void> endAsCompleted(ShowCommandResponse.Ack response) {
    if(response.error() == ShowCommandError.NO_ERROR){
      return effects()
              .updateState(currentState().asCompleted())
              .end();
    }else{
      throw new IllegalStateException("Expecting successful response, but got: " + response.error());
    }
  }

  private DeferredCall<Any, ShowCommandResponse.Ack> cancelReservation() {
    logger.info("cancelling reservation");
    return componentClient.forEventSourcedEntity(currentState().showId())
            .call(ShowEntity::cancelSeatReservation)
            .params(currentState().reservationId());
  }

  private Effect.TransitionalEffect<Void> endAsFailed(ShowCommandResponse.Ack response) {
    if(response.error() == ShowCommandError.NO_ERROR){
      return effects()
              .updateState(currentState().asFailed(currentState().failReason().orElse("N/A")))
              .end();
    }else{
      throw new IllegalStateException("Expecting successful response, but got: " + response.error());
    }
  }


  private DeferredCall<Any, WalletCommandResponse.Ack> refund() {
    logger.info("refunding");
    //we can't use reservationId for refund, because it was used for charging.
    var chargeExpenseId = currentState().reservationId();
    var refundExpenseId = UUID.nameUUIDFromBytes(currentState().reservationId().getBytes(UTF_8)).toString();
    return componentClient.forEventSourcedEntity(currentState().walletId())
      .call(WalletEntity::refundWalletCharge)
      .params(new WalletCommand.Refund(chargeExpenseId, refundExpenseId));
  }

  private Effect.TransitionalEffect<Void> cancelReservation(WalletCommandResponse.Ack response) {
    if(response.error() == WalletCommandError.NO_ERROR){
      return effects()
              .updateState(currentState().asWalletRefunded())
              .transitionTo(CANCEL_RESERVATION_STEP);
    }else{
      throw new IllegalStateException("Expecting successful response, but got: " + response.error());
    }
  }
}
