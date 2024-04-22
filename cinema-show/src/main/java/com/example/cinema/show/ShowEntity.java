package com.example.cinema.show;

import com.example.cinema.util.StateCommandProcessResult;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Id("id")
@TypeId("cinema-show")
@RequestMapping("/cinema-show/{id}")
public class ShowEntity extends EventSourcedEntity<ShowState, ShowEvent> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private String showId;

  public ShowEntity(EventSourcedEntityContext context) {
    this.showId = context.entityId();
  }

  @Override
  public ShowState emptyState() {
    return ShowState.empty();
  }

  @PostMapping
  public Effect<ShowCommandResponse.Ack> createShow(@PathVariable String id, @RequestBody ShowCommand.CreateShow createShow) {
    logger.info("createShow: showId[{}], createShow[{}]",showId, createShow);
    var result = currentState().handleCommand(id, createShow);
    return handleStateCommandProcessResult(result);
  }

  @PatchMapping("/reserve")
  public Effect<ShowCommandResponse.ShowReserveCommandResponse> reserveSeat(@RequestBody ShowCommand.ReserveSeat reserveSeat) {
    logger.info("reserveSeat: showId[{}], reserveSeat[{}]", showId,reserveSeat);
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.ShowReserveCommandResponse.error(ShowCommandError.SHOW_NOT_FOUND));
    }
    var result = currentState().handleCommand(reserveSeat);
    return handleStateCommandProcessResult(result, reserveSeat);
  }

  @PatchMapping("/cancel-reservation/{reservationId}")
  public Effect<ShowCommandResponse.Ack> cancelSeatReservation(@PathVariable String reservationId) {
    logger.info("cancelSeatReservation: showId[{}], reservationId[{}]", showId,reservationId);
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.Ack.error(ShowCommandError.SHOW_NOT_FOUND));
    }
    var cancelReservation = new ShowCommand.CancelSeatReservation(reservationId);
    var result = currentState().handleCommand(cancelReservation);
    return handleStateCommandProcessResult(result);
  }

  @PatchMapping("/confirm-payment/{reservationId}")
  public Effect<ShowCommandResponse.Ack> confirmSeatReservationPayment(@PathVariable String reservationId) {
    logger.info("confirmSeatReservationPayment: showId[{}], reservationId[{}]", showId,reservationId);
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.Ack.error(ShowCommandError.SHOW_NOT_FOUND));
    }
    var confirmReservationPayment = new ShowCommand.ConfirmReservationPayment(reservationId);
    var result = currentState().handleCommand(confirmReservationPayment);
    return handleStateCommandProcessResult(result);
  }

  @GetMapping
  public Effect<ShowCommandResponse.ShowSummeryResponse> get() {
    logger.info("get: showId[{}]", showId);
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.ShowSummeryResponse.error(currentState().id(),ShowCommandError.SHOW_NOT_FOUND));
    } else {
      return effects().reply(new ShowCommandResponse.ShowSummeryResponse(currentState().id(), currentState().title(), currentState().pendingReservations().size(), currentState().payedReservations().size(), currentState().availableSeatsCount(), ShowCommandError.NO_ERROR));
    }
  }

  @GetMapping("/seat-status/{seatNumber}")
  public Effect<ShowCommandResponse.ShowSeatStatusCommandResponse> getSeatStatus(@PathVariable int seatNumber) {
    logger.info("get: showId[{}], seatNumber[{}]", showId, seatNumber);
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.ShowSeatStatusCommandResponse.error(currentState().id(),seatNumber,ShowCommandError.SHOW_NOT_FOUND));
    } else {
      return effects().reply(Optional.ofNullable(currentState().seats().get(seatNumber))
                            .map(seat -> new ShowCommandResponse.ShowSeatStatusCommandResponse(currentState().id(), seatNumber,currentState().seatPrice(), seat.walletId().orElse(null),seat.status(),ShowCommandError.NO_ERROR))
                            .orElse(ShowCommandResponse.ShowSeatStatusCommandResponse.error(currentState().id(),seatNumber, ShowCommandError.SEAT_NOT_FOUND)));
    }
  }

  @EventHandler
  public ShowState onEvent(ShowEvent.ShowCreated event) {
    return currentState().onEvent(event);
  }

  @EventHandler
  public ShowState onEvent(ShowEvent.SeatReserved seatReserved) {
    return currentState().onEvent(seatReserved);
  }

  @EventHandler
  public ShowState onEvent(ShowEvent.SeatReservationCancelled event) {
    return currentState().onEvent(event);
  }

  @EventHandler
  public ShowState onEvent(ShowEvent.SeatReservationPaid event) {
    return currentState().onEvent(event);
  }

  private Effect<ShowCommandResponse.Ack> handleStateCommandProcessResult(StateCommandProcessResult<ShowEvent, ShowCommandError> result){
    if(!result.events().isEmpty()){
      return effects().emitEvents(result.events())
              .thenReply(updateState ->
                      result.error()
                              .map(error -> ShowCommandResponse.Ack.error(error))
                              .orElse(ShowCommandResponse.Ack.ok())
              );
    }else{
      return effects().reply(
              result.error()
                      .map(error -> ShowCommandResponse.Ack.error(error))
                      .orElse(ShowCommandResponse.Ack.ok())
              );
    }
  }

  private Effect<ShowCommandResponse.ShowReserveCommandResponse> handleStateCommandProcessResult(StateCommandProcessResult<ShowEvent, ShowCommandError> result, ShowCommand.ReserveSeat reserveSeat){
    if(!result.events().isEmpty()){
      return effects().emitEvents(result.events())
              .thenReply(updateState ->
                      result.error()
                              .map(error -> ShowCommandResponse.ShowReserveCommandResponse.error(error))
                              .orElse(ShowCommandResponse.ShowReserveCommandResponse.ok(reserveSeat.seatNumber(),updateState.seatPrice()))
              );
    }else{
      return effects().reply(
              result.error()
                      .map(error -> ShowCommandResponse.ShowReserveCommandResponse.error(error))
                      .orElse(ShowCommandResponse.ShowReserveCommandResponse.ok(reserveSeat.seatNumber(),currentState().seatPrice()))
      );
    }
  }

}
