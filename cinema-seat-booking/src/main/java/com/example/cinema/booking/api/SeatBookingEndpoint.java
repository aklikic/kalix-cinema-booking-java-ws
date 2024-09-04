package com.example.cinema.booking.api;

import akka.javasdk.annotations.http.Endpoint;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.example.cinema.booking.application.SeatBookingWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

@Endpoint("/seat-booking")
public class SeatBookingEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(SeatBookingEndpoint.class);

    private final ComponentClient componentClient;

    public SeatBookingEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Post("/{id}")
    public CompletionStage<SeatBookingCommandResponse> start(String id, SeatBookingCommand bookSeat) {
        logger.info("start: reservationId=[{}], bookSeat=[{}]",id,bookSeat);
        return componentClient.forWorkflow(id).method(SeatBookingWorkflow::start).invokeAsync(bookSeat);
    }

    @Get("/{id}")
    public CompletionStage<SeatBookingCommandResponse> getState(String id) {
        logger.info("getState: reservationId[{}]",id);
        return componentClient.forWorkflow(id).method(SeatBookingWorkflow::getState).invokeAsync();
    }
}
