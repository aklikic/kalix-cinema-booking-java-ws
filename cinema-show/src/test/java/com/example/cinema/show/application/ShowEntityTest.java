package com.example.cinema.show.application;

import akka.javasdk.testkit.EventSourcedResult;
import akka.javasdk.testkit.EventSourcedTestKit;
import com.example.cinema.show.SeatStatus;
import com.example.cinema.show.ShowCommand;
import com.example.cinema.show.ShowCommandResponse;
import com.example.cinema.show.domain.ShowEvent;
import com.example.cinema.show.domain.ShowState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShowEntityTest {

  @Test
  public void shouldReserveAndConfirmSeat() {
    //given
    var showId = UUID.randomUUID().toString();
    var seatPrice = new BigDecimal(100);
    var maxSeats = 10;
    var seatNumberToReserve = 3;
    var reservationId = UUID.randomUUID().toString();

    EventSourcedTestKit<ShowState, ShowEvent, ShowEntity> testKit = EventSourcedTestKit.of(showId,ShowEntity::new);
    var createShow = new ShowCommand.CreateShow("title",seatPrice, maxSeats);
    var reserveSeat = new ShowCommand.ReserveSeat(showId, reservationId, seatNumberToReserve);

    //when
    testKit.call(s -> s.createShow(createShow));
    testKit.call(s -> s.reserveSeat(reserveSeat));
    EventSourcedResult<ShowCommandResponse.Ack> result = testKit.call(s -> s.confirmSeatReservationPayment(reservationId));

    //then
    var confirmedSeat = testKit.getState().seats().get(seatNumberToReserve);
    assertEquals(seatNumberToReserve, confirmedSeat.number());
    assertEquals(SeatStatus.PAID, confirmedSeat.status());

    assertEquals(maxSeats-1, testKit.getState().availableSeatsCount());
  }
}