package com.example.cinema.booking.api;

import com.example.cinema.booking.domain.SeatBookingState;

import java.util.Optional;

public record SeatBookingCommandResponse(Optional<SeatBookingState> state, SeatBookingCommandError error) {
    public static SeatBookingCommandResponse ok (SeatBookingState state){
        return new SeatBookingCommandResponse(Optional.of(state), SeatBookingCommandError.NO_ERROR);
    }
    public static SeatBookingCommandResponse error (SeatBookingCommandError error){
        return new SeatBookingCommandResponse(Optional.empty(),error);
    }
    public static SeatBookingCommandResponse error (SeatBookingState state,SeatBookingCommandError error){
        return new SeatBookingCommandResponse(Optional.of(state),error);
    }
}
