package com.example.cinema.booking;

import java.util.Optional;

public record SeatBookingCommandResponse(Optional<SeatBookingState> state, SeatBookingCommandError error) {
    public static SeatBookingCommandResponse ok (SeatBookingState state){
        return new SeatBookingCommandResponse(Optional.of(state), SeatBookingCommandError.NO_ERROR);
    }
    public static SeatBookingCommandResponse error (SeatBookingCommandError error){
        return new SeatBookingCommandResponse(Optional.empty(),error);
    }
}
