package com.example.cinema.show.domain;

import com.example.cinema.show.SeatStatus;
import com.example.cinema.show.ShowCommand;
import com.example.cinema.show.ShowCommandError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShowStateTest {

  @Test
  public void shouldCreateTheShow() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);

    //when
    var result = show.handleCommand(showId, createShow);
    assertTrue(result.error().isEmpty());
    assertEquals(1, result.events().size());
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //then
    assertEquals(showId, updatedShow.id());
    assertEquals(createShow.maxSeats(), updatedShow.availableSeatsCount());
  }

  @Test
  public void shouldNotProcessCreateShowCommandForExistingShow() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //when
    result = updatedShow.handleCommand(showId, createShow);

    //then
    assertEquals(ShowCommandError.SHOW_ALREADY_EXISTS,result.error().orElse(ShowCommandError.NO_ERROR));
  }

  @Test
  public void shouldReserveTheSeat() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //when
    var seatNumberToReserve = 3;
    var reservationId = UUID.randomUUID().toString();

    //reserve the show
    var reserveSeat = new ShowCommand.ReserveSeat(showId, reservationId, seatNumberToReserve);
    result = updatedShow.handleCommand(reserveSeat);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReserved) event);

    //then
    assertEquals(maxSeats-1, updatedShow.availableSeatsCount());
    assertEquals(1, updatedShow.pendingReservations().size());
    assertTrue(seatNumberToReserve == updatedShow.pendingReservations().get(reservationId));
    assertEquals(SeatStatus.RESERVED,updatedShow.seats().get(seatNumberToReserve).status());
  }


  @Test
  public void shouldNotReserveAlreadyReservedSeat() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //reserve the show
    var seatNumberToReserve = 3;
    var reservationId = UUID.randomUUID().toString();
    var reserveSeat = new ShowCommand.ReserveSeat(showId, reservationId, seatNumberToReserve);
    result = updatedShow.handleCommand(reserveSeat);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReserved) event);

    //when
    var newReservationId = UUID.randomUUID().toString();
    var newReserveSeat = new ShowCommand.ReserveSeat(showId, newReservationId, seatNumberToReserve);
    result = updatedShow.handleCommand(newReserveSeat);

    //then
    assertEquals(ShowCommandError.SEAT_NOT_AVAILABLE, result.error().orElse(ShowCommandError.NO_ERROR));
  }

  @Test
  public void shouldRejectReservationDuplicate() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //reserve the show
    var seatNumberToReserve = 3;
    var reservationId = UUID.randomUUID().toString();
    var reserveSeat = new ShowCommand.ReserveSeat(showId, reservationId, seatNumberToReserve);
    result = updatedShow.handleCommand(reserveSeat);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReserved) event);

    //when
    result = updatedShow.handleCommand(reserveSeat);

    //then
    assertEquals(ShowCommandError.DUPLICATED_COMMAND, result.error().orElse(ShowCommandError.NO_ERROR));
  }

  @Test
  public void shouldNotReserveNotExistingSeat() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //when
    var notExistingSeatNumberToReserve = maxSeats+10;
    var reservationId = UUID.randomUUID().toString();

    var reserveSeat = new ShowCommand.ReserveSeat(showId, reservationId, notExistingSeatNumberToReserve);
    result = updatedShow.handleCommand(reserveSeat);

    //then
    assertEquals(ShowCommandError.SEAT_NOT_FOUND, result.error().orElse(ShowCommandError.NO_ERROR));
  }

  @Test
  public void shouldCancelSeatReservation() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;
    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //reserve the show
    var seatNumberToReserve = 3;
    var reservationId = UUID.randomUUID().toString();

    var reserveSeat = new ShowCommand.ReserveSeat(showId, reservationId, seatNumberToReserve);
    result = updatedShow.handleCommand(reserveSeat);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReserved) event);

    //when
    var cancelSeatReservation = new ShowCommand.CancelSeatReservation(reservationId);
    result = updatedShow.handleCommand(cancelSeatReservation);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReservationCancelled) event);

    //then
    assertEquals(maxSeats, updatedShow.availableSeatsCount());
    assertEquals(0, updatedShow.pendingReservations().size());
    assertEquals(SeatStatus.AVAILABLE,updatedShow.seats().get(seatNumberToReserve).status());
  }

  @Test
  public void shouldRejectCancellationDuplicate() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //reserve the show
    var seatNumberToReserve = 3;
    var reservationId = UUID.randomUUID().toString();

    var reserveSeat = new ShowCommand.ReserveSeat(showId, reservationId, seatNumberToReserve);
    result = updatedShow.handleCommand(reserveSeat);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReserved) event);

    //cancel the reservation
    var cancelSeatReservation = new ShowCommand.CancelSeatReservation(reservationId);
    result = updatedShow.handleCommand(cancelSeatReservation);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReservationCancelled) event);

    //when
    result = updatedShow.handleCommand(cancelSeatReservation);

    //then
    assertEquals(ShowCommandError.RESERVATION_NOT_FOUND,result.error().orElse(ShowCommandError.NO_ERROR));
  }

  @Test
  public void shouldConfirmSeatReservation() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //reserve the show
    var seatNumberToReserve = 3;
    var reservationId = UUID.randomUUID().toString();

    var reserveSeat = new ShowCommand.ReserveSeat(showId, reservationId, seatNumberToReserve);
    result = updatedShow.handleCommand(reserveSeat);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReserved) event);

    //when
    var confirmPayment = new ShowCommand.ConfirmReservationPayment(reservationId);
    result = updatedShow.handleCommand(confirmPayment);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReservationPaid) event);

    //then
    assertEquals(maxSeats-1, updatedShow.availableSeatsCount());
    assertEquals(0, updatedShow.pendingReservations().size());
    assertEquals(1, updatedShow.payedReservations().size());
    assertEquals(reservationId, updatedShow.seats().get(seatNumberToReserve).reservationId().orElse(null));
    assertEquals(SeatStatus.PAID,updatedShow.seats().get(seatNumberToReserve).status());
  }

  @Test
  public void shouldRejectConfirmationDuplicate() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //reserve the show
    var seatNumberToReserve = 3;
    var reservationId = UUID.randomUUID().toString();
    var reserveSeat = new ShowCommand.ReserveSeat(showId, reservationId, seatNumberToReserve);
    result = updatedShow.handleCommand(reserveSeat);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReserved) event);

    // confirm reservation payment
    var confirmPayment = new ShowCommand.ConfirmReservationPayment(reservationId);
    result = updatedShow.handleCommand(confirmPayment);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReservationPaid) event);

    //when
    result = updatedShow.handleCommand(confirmPayment);

    //then
    assertEquals(ShowCommandError.RESERVATION_NOT_FOUND,result.error().orElse(ShowCommandError.NO_ERROR));
  }

  @Test
  public void shouldRejectCancellationAfterConfirmation() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //reserve the show
    var seatNumberToReserve = 3;
    var reservationId = UUID.randomUUID().toString();
    var reserveSeat = new ShowCommand.ReserveSeat(showId, reservationId, seatNumberToReserve);
    result = updatedShow.handleCommand(reserveSeat);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReserved) event);

    // confirm reservation payment
    var confirmPayment = new ShowCommand.ConfirmReservationPayment(reservationId);
    result = updatedShow.handleCommand(confirmPayment);
    event = result.events().get(0);
    updatedShow = updatedShow.onEvent((ShowEvent.SeatReservationPaid) event);

    //when
    var cancelSeatReservation = new ShowCommand.CancelSeatReservation(reservationId);
    result = updatedShow.handleCommand(cancelSeatReservation);

    //then
    assertEquals(ShowCommandError.CANCELLING_CONFIRMED_RESERVATION,result.error().orElse(ShowCommandError.NO_ERROR));

  }

  @Test
  public void shouldNotCancelReservationOfAvailableSeat() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //when
    var reservationId = UUID.randomUUID().toString();
    var cancelSeatReservation = new ShowCommand.CancelSeatReservation(reservationId);
    result = updatedShow.handleCommand(cancelSeatReservation);

    //then
    assertEquals(ShowCommandError.RESERVATION_NOT_FOUND,result.error().orElse(ShowCommandError.NO_ERROR));
  }

  @Test
  public void shouldNotConfirmSeatReservationOfAvailableSeat() {
    //given
    var show = ShowState.empty();
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;

    //create the show
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var result = show.handleCommand(showId, createShow);
    var event = result.events().get(0);
    var updatedShow = show.onEvent((ShowEvent.ShowCreated) event);

    //when
    var reservationId = UUID.randomUUID().toString();
    var cancelSeatReservation = new ShowCommand.CancelSeatReservation(reservationId);
    result = updatedShow.handleCommand(cancelSeatReservation);

    //then
    assertEquals(ShowCommandError.RESERVATION_NOT_FOUND,result.error().orElse(ShowCommandError.NO_ERROR));
  }

}