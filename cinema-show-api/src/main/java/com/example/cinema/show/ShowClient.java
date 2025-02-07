package com.example.cinema.show;

import akka.javasdk.http.HttpClient;
import akka.javasdk.http.StrictResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

public class ShowClient {

    private static final Logger logger = LoggerFactory.getLogger(ShowClient.class);
    final private HttpClient httpClient;

    public ShowClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletionStage<ShowCommandResponse.Ack> createShow(String showId, String title, BigDecimal seatPrice, int maxSeats) {
        logger.info("createShow: {}",showId);
        return httpClient.POST("/cinema-show/" + showId)
                .withRequestBody(new ShowCommand.CreateShow(title, seatPrice, maxSeats))
                .responseBodyAs(ShowCommandResponse.Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
    public CompletionStage<ShowCommandResponse.ShowReserveCommandResponse> reserveSeat(String showId, String walletId, String reservationId, int seatNumber) {
        logger.info("reserveSeat: showId[{}], reservationId[{}], walletId[{}], seatNumber[{}]",showId,reservationId, walletId, seatNumber);
        return httpClient.PATCH("/cinema-show/" + showId + "/reserve")
                .withRequestBody(new ShowCommand.ReserveSeat(walletId, reservationId, seatNumber))
                .responseBodyAs(ShowCommandResponse.ShowReserveCommandResponse.class)
                .invokeAsync()
                .thenApply(StrictResponse::body)
                .exceptionally(e->{
                    logger.error("Error on reserveSeat: {}",e);
                    throw (RuntimeException)e;
                });
    }
    public CompletionStage<ShowCommandResponse.Ack> cancelSeatReservation(String showId, String reservationId) {
        logger.info("cancelSeatReservation: showId[{}], reservationId[{}]",showId,reservationId);
        return httpClient.PATCH("/cinema-show/" + showId + "/cancel-reservation/" + reservationId)
                .responseBodyAs(ShowCommandResponse.Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }

    public CompletionStage<ShowCommandResponse.Ack> confirmSeatReservationPayment(String showId, String reservationId) {
        logger.info("confirmSeatReservationPayment: showId[{}], reservationId[{}]",showId,reservationId);
        return httpClient.PATCH("/cinema-show/" + showId + "/confirm-payment/" + reservationId)
                .responseBodyAs(ShowCommandResponse.Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }

    public CompletionStage<ShowCommandResponse.ShowSummeryResponse> get(String showId) {
        logger.info("get: showId[{}]",showId);
        return httpClient.GET("/cinema-show/" + showId)
                .responseBodyAs(ShowCommandResponse.ShowSummeryResponse.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
    public CompletionStage<ShowCommandResponse.ShowSeatStatusCommandResponse> getSeatStatus(String showId, int seatNumber) {
        logger.info("confirmSeatReservationPayment: showId[{}], seatNumber[{}]",showId,seatNumber);
        return httpClient.GET("/cinema-show/" + showId + "/seat-status/" + seatNumber)
                .responseBodyAs(ShowCommandResponse.ShowSeatStatusCommandResponse.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }


}
