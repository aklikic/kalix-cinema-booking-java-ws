package com.example.cinema.show;

import com.example.cinema.util.StateCommandProcessResult;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Id("id")
@TypeId("cinema-show")
@RequestMapping("/cinema-show/{id}")
public class ShowEntity extends EventSourcedEntity<ShowState, ShowEvent> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public ShowState emptyState() {
    return ShowState.empty();
  }

  @PostMapping
  public Effect<ShowCommandResponse.Ack> createShow(@PathVariable String id, @RequestBody ShowCommand.CreateShow createShow) {
    var result = currentState().handleCommand(id, createShow);
    return handleStateCommandProcessResult(result);
  }

  @PatchMapping("/reserve")
  public Effect<ShowCommandResponse.ShowReserveCommandResponse> reserveSeat(@RequestBody ShowCommand.ReserveSeat reserveSeat) {
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.ShowReserveCommandResponse.error(ShowCommandError.SHOW_NOT_FOUND));
    }
    var result = currentState().handleCommand(reserveSeat);
    return handleStateCommandProcessResult(result, reserveSeat);
  }

  @PatchMapping("/cancel-reservation/{reservationId}")
  public Effect<ShowCommandResponse.Ack> cancelSeatReservation(@PathVariable String reservationId) {
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.Ack.error(ShowCommandError.SHOW_NOT_FOUND));
    }
    var cancelReservation = new ShowCommand.CancelSeatReservation(reservationId);
    var result = currentState().handleCommand(cancelReservation);
    return handleStateCommandProcessResult(result);
  }

  @PatchMapping("/confirm-payment/{reservationId}")
  public Effect<ShowCommandResponse.Ack> confirmSeatReservationPayment(@PathVariable String reservationId) {
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.Ack.error(ShowCommandError.SHOW_NOT_FOUND));
    }
    var confirmReservationPayment = new ShowCommand.ConfirmReservationPayment(reservationId);
    var result = currentState().handleCommand(confirmReservationPayment);
    return handleStateCommandProcessResult(result);
  }

  @GetMapping
  public Effect<ShowCommandResponse.ShowSummeryResponse> get() {
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.ShowSummeryResponse.error(currentState().id(),ShowCommandError.SHOW_NOT_FOUND));
    } else {
      return effects().reply(ShowCommandResponse.ShowSummeryResponse.ok(currentState()));
    }
  }

  @GetMapping("/seat-status/{seatNumber}")
  public Effect<ShowCommandResponse.ShowSeatStatusCommandResponse> getSeatStatus(@PathVariable int seatNumber) {
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.ShowSeatStatusCommandResponse.error(currentState().id(),seatNumber,ShowCommandError.SHOW_NOT_FOUND));
    } else {
      return effects().reply(ShowCommandResponse.ShowSeatStatusCommandResponse.ok(currentState(),seatNumber));
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
