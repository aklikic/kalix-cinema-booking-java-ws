package com.example.cinema.show;

import java.math.BigDecimal;
import java.util.Optional;

public interface ShowCommandResponse{

    ShowCommandError error();
    record Ack(ShowCommandError error) implements ShowCommandResponse {

        public static Ack ok() {
            return new Ack(ShowCommandError.NO_ERROR);
        }

        public static Ack error(ShowCommandError error) {
            return new Ack(error);
        }
    }

    record ShowSummeryResponse(String id, String title, int pendingReservationsCount, int payedReservationsCount, int availableSeats, ShowCommandError error) {
//        public static ShowSummeryResponse ok(ShowState show) {
//            return new ShowSummeryResponse(show.id(), show.title(), show.pendingReservations().size(), show.payedReservations().size(), show.availableSeatsCount(), ShowCommandError.NO_ERROR);
//        }
        public static ShowSummeryResponse error(String id, ShowCommandError error) {
            return new ShowSummeryResponse(id, "", 0,0,0,error);
        }
    }

    record ShowReserveCommandResponse(int seatNumber, BigDecimal price, ShowCommandError error) implements ShowCommandResponse{

        public static ShowReserveCommandResponse ok(int seatNumber, BigDecimal price) {
            return new ShowReserveCommandResponse(seatNumber, price, ShowCommandError.NO_ERROR);
        }
        public static ShowReserveCommandResponse error(ShowCommandError error) {
            return new ShowReserveCommandResponse( 0, BigDecimal.ZERO,error);
        }
    }

    record ShowSeatStatusCommandResponse(String showId, int seatNumber, SeatStatus seatStatus, ShowCommandError error) implements ShowCommandResponse{

//        public static ShowSeatStatusCommandResponse ok(ShowState show, int seatNumber) {
//            return  Optional.ofNullable(show.seats().get(seatNumber))
//                            .map(seat -> new ShowSeatStatusCommandResponse(show.id(), seatNumber,seat.status(),ShowCommandError.NO_ERROR))
//                            .orElse(error(show.id(),seatNumber, ShowCommandError.SEAT_NOT_FOUND));
//        }
        public static ShowSeatStatusCommandResponse error(String id, int seatNumber, ShowCommandError error) {
            return new ShowSeatStatusCommandResponse(id, seatNumber, null, error);
        }
    }

}
