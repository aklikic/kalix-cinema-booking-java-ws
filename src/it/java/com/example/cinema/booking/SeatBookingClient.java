package com.example.cinema.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletionStage;


@Component
public class SeatBookingClient {

  @Autowired
  private WebClient webClient;

  public CompletionStage<SeatBookingCommandResponse> start(String reservationId, String showId, int seatNumber, String walletId) {
    return webClient.post().uri("/seat-booking/" + reservationId)
            .bodyValue(new SeatBookingCommand(showId,seatNumber,walletId))
            .retrieve()
            .bodyToMono(SeatBookingCommandResponse.class)
            .toFuture();
  }
  public CompletionStage<SeatBookingCommandResponse> get(String reservationId) {
    return webClient.get().uri("/seat-booking/" + reservationId)
            .retrieve()
            .bodyToMono(SeatBookingCommandResponse.class)
            .toFuture();
  }

}
