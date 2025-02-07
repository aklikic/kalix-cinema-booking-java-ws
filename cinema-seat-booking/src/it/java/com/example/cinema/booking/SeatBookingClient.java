package com.example.cinema.booking;

import akka.javasdk.client.ComponentClient;
import com.example.cinema.booking.api.SeatBookingCommand;
import com.example.cinema.booking.api.SeatBookingCommandResponse;
import com.example.cinema.booking.application.SeatBookingWorkflow;

import java.util.concurrent.CompletionStage;

public class SeatBookingClient {

    private final ComponentClient componentClient;

    public SeatBookingClient(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public CompletionStage<SeatBookingCommandResponse> start(String reservationId, String showId, int seatNumber, String walletId) {
        return componentClient.forWorkflow(reservationId)
                .method(SeatBookingWorkflow::start)
                .invokeAsync(new SeatBookingCommand(showId,seatNumber,walletId));
    }
    public CompletionStage<SeatBookingCommandResponse> get(String reservationId) {
        return componentClient.forWorkflow(reservationId)
                .method(SeatBookingWorkflow::getState)
                .invokeAsync();
    }

}
