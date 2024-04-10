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
  public Effect<ShowCommandResponse> createShow(@PathVariable String id, @RequestBody ShowCommand.CreateShow createShow) {
    var result = currentState().handleCommand(id, createShow);
    return handleStateCommandProcessResult(result);
  }

  @PatchMapping("/reserve")
  public Effect<ShowCommandResponse> reserveSeat(@RequestBody ShowCommand.ReserveSeat reserveSeat) {
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.of(currentState().id(),ShowCommandError.SHOW_NOT_FOUND));
    }
    var result = currentState().handleCommand(reserveSeat);
    return handleStateCommandProcessResult(result);
  }

  @PatchMapping("/cancel-reservation/{reservationId}")
  public Effect<ShowCommandResponse> cancelSeatReservation(@PathVariable String reservationId) {
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.of(currentState().id(),ShowCommandError.SHOW_NOT_FOUND));
    }
    var cancelReservation = new ShowCommand.CancelSeatReservation(reservationId);
    var result = currentState().handleCommand(cancelReservation);
    return handleStateCommandProcessResult(result);
  }

  @PatchMapping("/confirm-payment/{reservationId}")
  public Effect<ShowCommandResponse> confirmSeatReservationPayment(@PathVariable String reservationId) {
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.of(currentState().id(),ShowCommandError.SHOW_NOT_FOUND));
    }
    var confirmReservationPayment = new ShowCommand.ConfirmReservationPayment(reservationId);
    var result = currentState().handleCommand(confirmReservationPayment);
    return handleStateCommandProcessResult(result);
  }

  @GetMapping
  public Effect<ShowCommandResponse> get() {
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.of(currentState().id(),ShowCommandError.SHOW_NOT_FOUND));
    } else {
      return effects().reply(ShowCommandResponse.of(currentState()));
    }
  }

  @GetMapping("/seat-status/{seatNumber}")
  public Effect<ShowSeatStatusCommandResponse> getSeatStatus(@PathVariable int seatNumber) {
    if (currentState().isEmpty()) {
      return effects().reply(ShowSeatStatusCommandResponse.of(currentState().id(),seatNumber,ShowCommandError.SHOW_NOT_FOUND));
    } else {
      return effects().reply(ShowSeatStatusCommandResponse.of(currentState(),seatNumber));
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

  private Effect<ShowCommandResponse> handleStateCommandProcessResult(StateCommandProcessResult<ShowEvent, ShowCommandError> result){
    if(!result.events().isEmpty()){
      return effects().emitEvents(result.events())
              .thenReply(updateState ->
                      result.error()
                              .map(error -> ShowCommandResponse.of(updateState.id(),error))
                              .orElse(ShowCommandResponse.of(updateState))
              );
    }else{
      return effects().reply(
              result.error()
                      .map(error -> ShowCommandResponse.of(currentState().id(),error))
                      .orElse(ShowCommandResponse.of(currentState()))
              );
    }
  }
}
