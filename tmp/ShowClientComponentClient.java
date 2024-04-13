package com.example.cinema.booking;

import com.example.cinema.client.ShowClient;
import com.example.cinema.show.ShowCommand;
import com.example.cinema.show.ShowCommandResponse;
import com.example.cinema.show.ShowEntity;
import kalix.javasdk.client.ComponentClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

@Service
public class ShowClientComponentClient implements ShowClient {

    private final ComponentClient componentClient;

    public ShowClientComponentClient(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Override
    public CompletionStage<ShowCommandResponse.Ack> createShow(String showId, String title, BigDecimal seatPrice, int maxSeats) {
        return componentClient.forEventSourcedEntity(showId)
                .call(ShowEntity::createShow)
                .params(showId,new ShowCommand.CreateShow(title,seatPrice,maxSeats))
                .execute();
    }

    @Override
    public CompletionStage<ShowCommandResponse.ShowReserveCommandResponse> reserveSeat(String showId, String walletId, String reservationId, int seatNumber) {
        return componentClient.forEventSourcedEntity(showId)
                .call(ShowEntity::reserveSeat)
                .params(new ShowCommand.ReserveSeat(walletId, reservationId, seatNumber))
                .execute();
    }

    @Override
    public CompletionStage<ShowCommandResponse.Ack> cancelSeatReservation(String showId, String reservationId) {
        return componentClient.forEventSourcedEntity(showId)
                .call(ShowEntity::cancelSeatReservation)
                .params(reservationId)
                .execute();
    }

    @Override
    public CompletionStage<ShowCommandResponse.Ack> confirmSeatReservationPayment(String showId, String reservationId) {
        return componentClient.forEventSourcedEntity(showId)
                .call(ShowEntity::confirmSeatReservationPayment)
                .params(reservationId)
                .execute();
    }

    @Override
    public CompletionStage<ShowCommandResponse.ShowSummeryResponse> get(String showId) {
        return componentClient.forEventSourcedEntity(showId)
                .call(ShowEntity::get)
                .execute();
    }

    @Override
    public CompletionStage<ShowCommandResponse.ShowSeatStatusCommandResponse> getSeatStatus(String showId, int seatNumber) {
        return componentClient.forEventSourcedEntity(showId)
                .call(ShowEntity::getSeatStatus)
                .params(seatNumber)
                .execute();
    }
}
