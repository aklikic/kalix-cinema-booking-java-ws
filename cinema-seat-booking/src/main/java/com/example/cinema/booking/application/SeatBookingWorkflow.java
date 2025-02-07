package com.example.cinema.booking.application;


import akka.javasdk.annotations.ComponentId;
import akka.javasdk.http.HttpClientProvider;
import akka.javasdk.workflow.Workflow;
import akka.javasdk.workflow.WorkflowContext;
import com.example.cinema.booking.api.SeatBookingCommand;
import com.example.cinema.booking.api.SeatBookingCommandError;
import com.example.cinema.booking.api.SeatBookingCommandResponse;
import com.example.cinema.booking.domain.SeatBookingState;
import com.example.cinema.show.ShowClient;
import com.example.cinema.show.ShowCommandError;
import com.example.cinema.show.ShowCommandResponse;
import com.example.cinema.wallet.WalletClient;
import com.example.cinema.wallet.WalletCommandError;
import com.example.cinema.wallet.WalletCommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;

@ComponentId("seat-booking")
public class SeatBookingWorkflow extends Workflow<SeatBookingState> {

  public static final String RESERVE_SEAT_STEP = "reserve-seat";
  public static final String CHARGE_WALLET_STEP = "charge-wallet";
  public static final String CANCEL_RESERVATION_STEP = "cancel-reservation";
  public static final String CONFIRM_RESERVATION_STEP = "confirm-reservation";
  public static final String REFUND_STEP = "refund";
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final ShowClient showClient;
  private final WalletClient walletClient;

  public SeatBookingWorkflow(HttpClientProvider httpClientProvider) {
    this.showClient = new ShowClient(httpClientProvider.httpClientFor("cinema-show"));
    this.walletClient = new WalletClient(httpClientProvider.httpClientFor("cinema-wallet"));
  }

  private String reservationId() {
    return commandContext().workflowId();
  }


  public Effect<SeatBookingCommandResponse> start(SeatBookingCommand bookSeat) {
    logger.info("start: reservationId=[{}], bookSeat=[{}]",bookSeat);
    if (currentState() != null) {
      return effects().reply(SeatBookingCommandResponse.error(currentState(), SeatBookingCommandError.BOOKING_ALREADY_EXISTS));
    } else {
      var updatedState = SeatBookingState.of(reservationId(), bookSeat.showId(), bookSeat.seatNumber(), bookSeat.walletId());
      return effects()
              .updateState(updatedState)
              .transitionTo(RESERVE_SEAT_STEP)
              .thenReply(SeatBookingCommandResponse.ok(updatedState));
    }
  }

  public Effect<SeatBookingCommandResponse> getState() {
    logger.info("getState: reservationId[{}]",reservationId());
    if (currentState() == null) {
      return effects().reply(SeatBookingCommandResponse.error(SeatBookingCommandError.BOOKING_NOT_FOUND));
    } else {
      return effects().reply(SeatBookingCommandResponse.ok(currentState()));
    }
  }

  @Override
  public WorkflowDef<SeatBookingState> definition() {
    var reserveSeat = step(RESERVE_SEAT_STEP)
      .asyncCall(()->showClient.reserveSeat(currentState().showId(), currentState().walletId(), currentState().reservationId(), currentState().seatNumber()))
      .andThen(ShowCommandResponse.ShowReserveCommandResponse.class, this::chargeWalletOrStop);

    var chargeWallet = step(CHARGE_WALLET_STEP)
      .asyncCall(()->walletClient.chargeWallet(currentState().walletId(),currentState().price().get(), currentState().reservationId(), currentState().showId()))
      .andThen(WalletCommandResponse.Ack.class, this::confirmOrCancelReservation);

    var confirmReservation = step(CONFIRM_RESERVATION_STEP)
      .asyncCall(()->showClient.confirmSeatReservationPayment(currentState().showId(), currentState().reservationId()))
      .andThen(ShowCommandResponse.Ack.class, this::endAsCompleted);

    var cancelReservation = step(CANCEL_RESERVATION_STEP)
      .asyncCall(()->showClient.cancelSeatReservation(currentState().showId(), currentState().reservationId()))
      .andThen(ShowCommandResponse.Ack.class, this::endAsFailed);

    var refund = step(REFUND_STEP)
      .asyncCall(()->walletClient.refundWalletCharge(currentState().walletId(), currentState().reservationId(), UUID.randomUUID().toString()))
      .andThen(WalletCommandResponse.Ack.class, this::cancelReservation);

    return workflow()
      .defaultStepTimeout(Duration.ofSeconds(3))
      .addStep(reserveSeat, maxRetries(3).failoverTo(CANCEL_RESERVATION_STEP))
      .addStep(chargeWallet, maxRetries(3).failoverTo(REFUND_STEP))
      .addStep(confirmReservation)
      .addStep(cancelReservation)
      .addStep(refund);
  }

  private Effect.TransitionalEffect<Void> chargeWalletOrStop(ShowCommandResponse.ShowReserveCommandResponse response) {
    if(response.error() == ShowCommandError.NO_ERROR){
      return effects().updateState(currentState().asSeatReserved(response.price()))
              .transitionTo(CHARGE_WALLET_STEP);
    }else{
      return effects().updateState(currentState().asSeatBookingStateFailed(response.error().name())).end();
    }
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

  private Effect.TransitionalEffect<Void> endAsCompleted(ShowCommandResponse.Ack response) {
    if(response.error() == ShowCommandError.NO_ERROR){
      return effects()
              .updateState(currentState().asCompleted())
              .end();
    }else{
      throw new IllegalStateException("Expecting successful response, but got: " + response.error());
    }
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

  private Effect.TransitionalEffect<Void> cancelReservation(WalletCommandResponse.Ack response) {
    if(response.error() == WalletCommandError.NO_ERROR){
      return effects()
              .updateState(currentState().asWalletRefunded())
              .transitionTo(CANCEL_RESERVATION_STEP);
    }else{
      return effects()
              .transitionTo(CANCEL_RESERVATION_STEP);
    }
  }
}
