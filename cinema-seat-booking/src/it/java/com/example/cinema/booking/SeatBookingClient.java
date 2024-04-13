package com.example.cinema.booking;

import kalix.javasdk.client.ComponentClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

@Component
public class SeatBookingClient {

    private final ComponentClient componentClient;

    public SeatBookingClient(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public CompletionStage<SeatBookingCommandResponse> start(String reservationId, String showId, int seatNumber, String walletId) {
        return componentClient.forWorkflow(reservationId)
                .call(SeatBookingWorkflow::start)
                .params(new SeatBookingCommand(showId,seatNumber,walletId))
                .execute();
    }
    public CompletionStage<SeatBookingCommandResponse> get(String reservationId) {
        return componentClient.forWorkflow(reservationId)
                .call(SeatBookingWorkflow::getState)
                .execute();
    }

}
