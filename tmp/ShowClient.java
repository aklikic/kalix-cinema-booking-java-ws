package com.example.cinema.client;

import com.example.cinema.show.ShowCommandResponse;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;


public interface ShowClient {

    CompletionStage<ShowCommandResponse.Ack> createShow(String showId, String title, BigDecimal seatPrice, int maxSeats) ;
    CompletionStage<ShowCommandResponse.ShowReserveCommandResponse> reserveSeat(String showId, String walletId, String reservationId, int seatNumber) ;
    CompletionStage<ShowCommandResponse.Ack> cancelSeatReservation(String showId, String reservationId) ;

    CompletionStage<ShowCommandResponse.Ack> confirmSeatReservationPayment(String showId, String reservationId);

    CompletionStage<ShowCommandResponse.ShowSummeryResponse> get(String showId) ;
    CompletionStage<ShowCommandResponse.ShowSeatStatusCommandResponse> getSeatStatus(String showId, int seatNumber) ;


}
