package com.example.cinema.show;

import java.math.BigDecimal;

sealed public interface ShowCommand {

    record CreateShow(String title, BigDecimal seatPrice, int maxSeats) implements ShowCommand {
    }

    record ReserveSeat(String walletId, String reservationId, int seatNumber) implements ShowCommand {
    }

    record ConfirmReservationPayment(String reservationId) implements ShowCommand {
    }

    record CancelSeatReservation(String reservationId) implements ShowCommand {
    }
}
