package com.example.cinema.show.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.example.cinema.show.*;
import com.example.cinema.show.domain.ShowEvent;
import com.example.cinema.show.domain.ShowState;
import com.example.cinema.util.StateCommandProcessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ComponentId("cinema-show")
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

  public Effect<ShowCommandResponse.Ack> createShow(ShowCommand.CreateShow createShow) {
    logger.info("createShow: showId[{}], createShow[{}]",showId, createShow);
    var result = currentState().handleCommand(showId, createShow);
    return handleStateCommandProcessResult(result);
  }

  public Effect<ShowCommandResponse.ShowReserveCommandResponse> reserveSeat(ShowCommand.ReserveSeat reserveSeat) {
    logger.info("reserveSeat: showId[{}], reserveSeat[{}]", showId,reserveSeat);
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.ShowReserveCommandResponse.error(ShowCommandError.SHOW_NOT_FOUND));
    }
    var result = currentState().handleCommand(reserveSeat);
    return handleStateCommandProcessResult(result, reserveSeat);
  }

  public Effect<ShowCommandResponse.Ack> cancelSeatReservation(String reservationId) {
    logger.info("cancelSeatReservation: showId[{}], reservationId[{}]", showId,reservationId);
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.Ack.error(ShowCommandError.SHOW_NOT_FOUND));
    }
    var cancelReservation = new ShowCommand.CancelSeatReservation(reservationId);
    var result = currentState().handleCommand(cancelReservation);
    return handleStateCommandProcessResult(result);
  }

  public Effect<ShowCommandResponse.Ack> confirmSeatReservationPayment(String reservationId) {
    logger.info("confirmSeatReservationPayment: showId[{}], reservationId[{}]", showId,reservationId);
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.Ack.error(ShowCommandError.SHOW_NOT_FOUND));
    }
    var confirmReservationPayment = new ShowCommand.ConfirmReservationPayment(reservationId);
    var result = currentState().handleCommand(confirmReservationPayment);
    return handleStateCommandProcessResult(result);
  }

  public Effect<ShowCommandResponse.ShowSummeryResponse> get() {
    logger.info("get: showId[{}]", showId);
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.ShowSummeryResponse.error(currentState().id(),ShowCommandError.SHOW_NOT_FOUND));
    } else {
      return effects().reply(new ShowCommandResponse.ShowSummeryResponse(currentState().id(), currentState().title(), currentState().pendingReservations().size(), currentState().payedReservations().size(), currentState().availableSeatsCount(), ShowCommandError.NO_ERROR));
    }
  }

  public Effect<ShowCommandResponse.ShowSeatStatusCommandResponse> getSeatStatus(int seatNumber) {
    logger.info("get: showId[{}], seatNumber[{}]", showId, seatNumber);
    if (currentState().isEmpty()) {
      return effects().reply(ShowCommandResponse.ShowSeatStatusCommandResponse.error(currentState().id(),seatNumber,ShowCommandError.SHOW_NOT_FOUND));
    } else {
      return effects().reply(Optional.ofNullable(currentState().seats().get(seatNumber))
                            .map(seat -> new ShowCommandResponse.ShowSeatStatusCommandResponse(currentState().id(), seatNumber,currentState().seatPrice(), seat.walletId().orElse(null),seat.status(),ShowCommandError.NO_ERROR))
                            .orElse(ShowCommandResponse.ShowSeatStatusCommandResponse.error(currentState().id(),seatNumber, ShowCommandError.SEAT_NOT_FOUND)));
    }
  }


  @Override
  public ShowState applyEvent(ShowEvent showEvent) {
    return switch (showEvent){
      case ShowEvent.ShowCreated evt -> currentState().onEvent(evt);
      case ShowEvent.SeatReserved evt -> currentState().onEvent(evt);
      case ShowEvent.SeatReservationPaid evt -> currentState().onEvent(evt);
      case ShowEvent.SeatReservationCancelled evt -> currentState().onEvent(evt);
    };
  }

  private Effect<ShowCommandResponse.Ack> handleStateCommandProcessResult(StateCommandProcessResult<ShowEvent, ShowCommandError> result){
    if(!result.events().isEmpty()){
      return effects().persistAll(result.events())
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
      return effects().persistAll(result.events())
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
