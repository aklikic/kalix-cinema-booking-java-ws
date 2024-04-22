package com.example.cinema.show;

import kalix.javasdk.annotations.TypeName;

import java.math.BigDecimal;

sealed public interface ShowPublicEvent {
    String showId();
    @TypeName("pub-seat-reserved")
    record SeatReserved(String showId, String walletId, String reservationId, int seatNumber, BigDecimal seatPrice) implements ShowPublicEvent {
    }
//    @TypeName("pub-seat-reservation-paid")
//    record SeatReservationPaid(String showId, String walletId, String reservationId, int seatNumber) implements ShowPublicEvent {}

    @TypeName("pub-seat-reservation-cancelled")
    record SeatReservationCancelled(String showId, String walletId, String reservationId, String reservationCancellationId, int seatNumber) implements ShowPublicEvent { }
}
