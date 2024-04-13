package com.example.cinema.booking;

import java.math.BigDecimal;
import java.util.Optional;

public record SeatBookingState(String reservationId, String showId, int seatNumber, String walletId,
                               SeatBookingStateStatus status, Optional<BigDecimal> price, Optional<String> failReason) {


    public static SeatBookingState of(String reservationId, String showId, int seatNumber, String walletId){
        return new SeatBookingState(reservationId, showId, seatNumber, walletId,  SeatBookingStateStatus.STARTED,Optional.empty(), Optional.empty());
    }
    public SeatBookingState asSeatBookingStateFailed(String reason) {
        return new SeatBookingState(reservationId, showId, seatNumber, walletId, status, price, Optional.ofNullable(reason));
    }

    public SeatBookingState asSeatReserved(BigDecimal price) {
        return new SeatBookingState(reservationId, showId, seatNumber, walletId,SeatBookingStateStatus.SEAT_RESERVED, Optional.ofNullable(price), failReason);
    }

    public SeatBookingState asWalletChargeRejected(String reason) {
        return new SeatBookingState(reservationId, showId, seatNumber, walletId, SeatBookingStateStatus.WALLET_CHARGE_REJECTED, price, Optional.ofNullable(reason));
    }

    public SeatBookingState asWalletCharged() {
        return new SeatBookingState(reservationId, showId, seatNumber, walletId, SeatBookingStateStatus.WALLET_CHARGED, price, failReason);
    }

    public SeatBookingState asCompleted() {
        return new SeatBookingState(reservationId, showId, seatNumber, walletId,SeatBookingStateStatus.COMPLETED, price, failReason);
    }

    public SeatBookingState asSeatBookingStateRefunded() {
        return new SeatBookingState(reservationId, showId, seatNumber, walletId, SeatBookingStateStatus.SEAT_RESERVATION_REFUNDED,price, failReason);
    }

    public SeatBookingState asWalletRefunded() {
        return new SeatBookingState(reservationId, showId, seatNumber, walletId, SeatBookingStateStatus.WALLET_REFUNDED, price, failReason);
    }

    public SeatBookingState asFailed(String reason) {
        if (status == SeatBookingStateStatus.WALLET_CHARGE_REJECTED || status == SeatBookingStateStatus.STARTED) {
            return asSeatBookingStateFailed(reason);
        } else if (status == SeatBookingStateStatus.WALLET_REFUNDED) {
            return asSeatBookingStateRefunded();
        } else {
            throw new IllegalStateException("not supported failed state transition from: " + status);
        }
    }

    public enum SeatBookingStateStatus {
      STARTED, SEAT_RESERVED, WALLET_CHARGE_REJECTED, WALLET_CHARGED, COMPLETED, SEAT_RESERVATION_FAILED, WALLET_REFUNDED, SEAT_RESERVATION_REFUNDED
    }
}
