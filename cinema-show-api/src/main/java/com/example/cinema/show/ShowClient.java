package com.example.cinema.show;

import kalix.spring.WebClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

@Component
public class ShowClient {

    private static final Logger logger = LoggerFactory.getLogger(ShowClient.class);
    final private WebClient webClient;

    public ShowClient(@Autowired WebClientProvider webClientProvider) {
        this.webClient = webClientProvider.webClientFor("cinema-show");
    }

//    public ShowClient(WebClient webClient) {
//        this.webClient = webClient;
//    }

    public CompletionStage<ShowCommandResponse.Ack> createShow(String showId, String title, BigDecimal seatPrice, int maxSeats) {
        logger.info("createShow: {}",showId);
        return webClient.post().uri("/cinema-show/" + showId)
                .bodyValue(new ShowCommand.CreateShow(title, seatPrice, maxSeats))
                .retrieve()
                .bodyToMono(ShowCommandResponse.Ack.class)
                .toFuture();
    }
    public CompletionStage<ShowCommandResponse.ShowReserveCommandResponse> reserveSeat(String showId, String walletId, String reservationId, int seatNumber) {
        logger.info("reserveSeat: showId[{}], reservationId[{}], walletId[{}], seatNumber[{}]",showId,reservationId, walletId, seatNumber);
        return webClient.patch().uri("/cinema-show/" + showId + "/reserve")
                .bodyValue(new ShowCommand.ReserveSeat(walletId, reservationId, seatNumber))
                .retrieve()
                .bodyToMono(ShowCommandResponse.ShowReserveCommandResponse.class)
                .toFuture()
                .exceptionally(e->{
                    logger.error("Error on reserveSeat: {}",e);
                    throw (RuntimeException)e;
                });
    }
    public CompletionStage<ShowCommandResponse.Ack> cancelSeatReservation(String showId, String reservationId) {
        logger.info("cancelSeatReservation: showId[{}], reservationId[{}]",showId,reservationId);
        return webClient.patch().uri("/cinema-show/" + showId + "/cancel-reservation/" + reservationId)
                .retrieve()
                .bodyToMono(ShowCommandResponse.Ack.class)
                .toFuture();
    }

    public CompletionStage<ShowCommandResponse.Ack> confirmSeatReservationPayment(String showId, String reservationId) {
        logger.info("confirmSeatReservationPayment: showId[{}], reservationId[{}]",showId,reservationId);
        return webClient.patch().uri("/cinema-show/" + showId + "/confirm-payment/" + reservationId)
                .retrieve()
                .bodyToMono(ShowCommandResponse.Ack.class)
                .toFuture();
    }

    public CompletionStage<ShowCommandResponse.ShowSummeryResponse> get(String showId) {
        logger.info("get: showId[{}]",showId);
        return webClient.get().uri("/cinema-show/" + showId)
                .retrieve()
                .bodyToMono(ShowCommandResponse.ShowSummeryResponse.class)
                .toFuture();
    }
    public CompletionStage<ShowCommandResponse.ShowSeatStatusCommandResponse> getSeatStatus(String showId, int seatNumber) {
        logger.info("confirmSeatReservationPayment: showId[{}], seatNumber[{}]",showId,seatNumber);
        return webClient.get().uri("/cinema-show/" + showId + "/seat-status/" + seatNumber)
                .retrieve()
                .bodyToMono(ShowCommandResponse.ShowSeatStatusCommandResponse.class)
                .toFuture();
    }


}
