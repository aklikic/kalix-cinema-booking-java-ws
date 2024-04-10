package com.example.cinema.show;

public record ShowCommandResponse(String id, String title, int pendingReservationsCount, int payedReservationsCount, int availableSeats, ShowCommandError error) {

    public static ShowCommandResponse of(ShowState show) {
        return new ShowCommandResponse(show.id(), show.title(), show.pendingReservations().size(), show.payedReservations().size(), show.availableSeatsCount(), ShowCommandError.NO_ERROR);
    }
    public static ShowCommandResponse of(String id, ShowCommandError error) {
        return new ShowCommandResponse(id, "", 0,0,0,error);
    }
}
