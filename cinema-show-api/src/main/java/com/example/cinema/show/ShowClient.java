package com.example.cinema.show;

import kalix.spring.WebClientProvider;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

public class ShowClient {

    final private WebClient webClient;

    public ShowClient(WebClientProvider webClientProvider) {
        this.webClient = webClientProvider.webClientFor("cinema-show");
    }

    public ShowClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public CompletionStage<ShowCommandResponse.Ack> createShow(String showId, String title, BigDecimal seatPrice, int maxSeats) {

        return webClient.post().uri("/cinema-show/" + showId)
                .bodyValue(new ShowCommand.CreateShow(title, seatPrice, maxSeats))
                .retrieve()
                .bodyToMono(ShowCommandResponse.Ack.class)
                .toFuture();
    }
    public CompletionStage<ShowCommandResponse.ShowReserveCommandResponse> reserveSeat(String showId, String walletId, String reservationId, int seatNumber) {
        return webClient.patch().uri("/cinema-show/" + showId + "/reserve")
                .bodyValue(new ShowCommand.ReserveSeat(walletId, reservationId, seatNumber))
                .retrieve()
                .bodyToMono(ShowCommandResponse.ShowReserveCommandResponse.class)
                .toFuture();
    }
    public CompletionStage<ShowCommandResponse.Ack> cancelSeatReservation(String showId, String reservationId) {
        return webClient.patch().uri("/cinema-show/" + showId + "/cancel-reservation/" + reservationId)
                .retrieve()
                .bodyToMono(ShowCommandResponse.Ack.class)
                .toFuture();
    }

    public CompletionStage<ShowCommandResponse.Ack> confirmSeatReservationPayment(String showId, String reservationId) {
        return webClient.patch().uri("/cinema-show/" + showId + "/confirm-payment/" + reservationId)
                .retrieve()
                .bodyToMono(ShowCommandResponse.Ack.class)
                .toFuture();
    }

    public CompletionStage<ShowCommandResponse.ShowSummeryResponse> get(String showId) {
        return webClient.get().uri("/cinema-show/" + showId)
                .retrieve()
                .bodyToMono(ShowCommandResponse.ShowSummeryResponse.class)
                .toFuture();
    }
    public CompletionStage<ShowCommandResponse.ShowSeatStatusCommandResponse> getSeatStatus(String showId, int seatNumber) {
        return webClient.get().uri("/cinema-show/" + showId + "/seat-status/" + seatNumber)
                .retrieve()
                .bodyToMono(ShowCommandResponse.ShowSeatStatusCommandResponse.class)
                .toFuture();
    }


}
