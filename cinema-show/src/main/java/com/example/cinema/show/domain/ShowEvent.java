package com.example.cinema.show.domain;


import akka.javasdk.annotations.TypeName;

import java.math.BigDecimal;
import java.util.List;

sealed public interface ShowEvent {
    String showId();
    @TypeName("show-created")
    record ShowCreated(String showId, String title, BigDecimal price, List<ShowState.Seat> seats) implements ShowEvent {
    }
    @TypeName("seat-reserved")
    record SeatReserved(String showId, String walletId, String reservationId, int seatNumber, BigDecimal seatPrice, int availableSeatsCount) implements ShowEvent {
    }
    @TypeName("seat-reservation-paid")
    record SeatReservationPaid(String showId, String reservationId, int seatNumber) implements ShowEvent {}

    @TypeName("seat-reservation-cancelled")
    record SeatReservationCancelled(String showId, String walletId, String reservationId, String reservationCancellationId, int seatNumber,int availableSeatsCount) implements ShowEvent { }
}
