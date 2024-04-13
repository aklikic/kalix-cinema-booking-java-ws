package com.example.cinema.client;

import com.example.cinema.booking.SeatBookingCommand;
import com.example.cinema.booking.SeatBookingCommandResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletionStage;


public class SeatBookingClient {

  final private WebClient webClient;

  public SeatBookingClient(WebClient webClient) {
    this.webClient = webClient;
  }

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
