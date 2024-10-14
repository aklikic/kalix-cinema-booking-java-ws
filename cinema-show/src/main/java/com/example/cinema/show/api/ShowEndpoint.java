package com.example.cinema.show.api;


import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Patch;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.example.cinema.show.ShowCommand;
import com.example.cinema.show.ShowCommandResponse;
import com.example.cinema.show.application.ShowEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;


@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
@HttpEndpoint("/cinema-show")
public class ShowEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ShowEndpoint.class);

    private final ComponentClient componentClient;

    public ShowEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }
    @Post("/{id}")
    public CompletionStage<ShowCommandResponse.Ack> createShow(String id, ShowCommand.CreateShow createShow) {
        logger.info("createShow: showId[{}], createShow[{}]",id, createShow);
        return componentClient.forEventSourcedEntity(id).method(ShowEntity::createShow).invokeAsync(createShow);
    }

    @Patch("/{id}/reserve")
    public CompletionStage<ShowCommandResponse.ShowReserveCommandResponse> reserveSeat(String id, ShowCommand.ReserveSeat reserveSeat) {
        logger.info("reserveSeat: showId[{}], reserveSeat[{}]", id,reserveSeat);
        return componentClient.forEventSourcedEntity(id).method(ShowEntity::reserveSeat).invokeAsync(reserveSeat);
    }

    @Patch("/{id}/cancel-reservation/{reservationId}")
    public CompletionStage<ShowCommandResponse.Ack> cancelSeatReservation(String id, String reservationId) {
        logger.info("cancelSeatReservation: showId[{}], reservationId[{}]", id,reservationId);
        return componentClient.forEventSourcedEntity(id).method(ShowEntity::cancelSeatReservation).invokeAsync(reservationId);
    }

    @Patch("/{id}/confirm-payment/{reservationId}")
    public CompletionStage<ShowCommandResponse.Ack> confirmSeatReservationPayment(String id, String reservationId) {
        logger.info("confirmSeatReservationPayment: showId[{}], reservationId[{}]", id,reservationId);
        return componentClient.forEventSourcedEntity(id).method(ShowEntity::confirmSeatReservationPayment).invokeAsync(reservationId);
    }

    @Get("/{id}")
    public CompletionStage<ShowCommandResponse.ShowSummeryResponse> get(String id) {
        logger.info("get: showId[{}]", id);
        return componentClient.forEventSourcedEntity(id).method(ShowEntity::get).invokeAsync();
    }

    @Get("/{id}/seat-status/{seatNumber}")
    public CompletionStage<ShowCommandResponse.ShowSeatStatusCommandResponse> getSeatStatus(String id, int seatNumber) {
        logger.info("get: showId[{}], seatNumber[{}]", id, seatNumber);
        return componentClient.forEventSourcedEntity(id).method(ShowEntity::getSeatStatus).invokeAsync(seatNumber);
    }
}
