package com.example.cinema.show;

import java.util.Optional;

public record ShowSeatStatusCommandResponse(String showId, int seatNumber, ShowState.SeatStatus seatStatus, ShowCommandError error) {

    public static ShowSeatStatusCommandResponse of(ShowState show, int seatNumber) {
        return
        Optional.ofNullable(show.seats().get(seatNumber))
                .map(seat -> new ShowSeatStatusCommandResponse(show.id(), seatNumber,seat.status(),ShowCommandError.NO_ERROR))
                .orElse(of(show.id(),seatNumber, ShowCommandError.SEAT_NOT_FOUND));
    }
    public static ShowSeatStatusCommandResponse of(String id,int seatNumber,  ShowCommandError error) {
        return new ShowSeatStatusCommandResponse(id, seatNumber, null, error);
    }
}
