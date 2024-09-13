package com.example.cinema.booking;


import akka.javasdk.http.HttpClientProvider;
import akka.javasdk.testkit.TestKitSupport;
import com.example.cinema.show.SeatStatus;
import com.example.cinema.show.ShowClient;
import com.example.cinema.show.ShowCommandError;
import com.example.cinema.wallet.WalletClient;
import com.example.cinema.wallet.WalletCommandError;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class SeatBookingChoreographyIntegrationTest extends TestKitSupport {


  private  WalletClient walletClient;
  private  ShowClient showClient;

    public SeatBookingChoreographyIntegrationTest(HttpClientProvider httpClientProvider) throws Exception{
      this.walletClient = new WalletClient(httpClientProvider.httpClientFor("cinema-wallet"));
      this.showClient = new ShowClient(httpClientProvider.httpClientFor("cinema-show"));
    }
    private static final Duration timeoutSec = Duration.of(10, SECONDS);

    @Test
  public void shouldCompleteSeatReservation() throws Exception{
    //given
    var walletId = UUID.randomUUID().toString();
    var initialBalance = 200;
    var showId = UUID.randomUUID().toString();
    var title = "title";
    var maxSeats = 10 ;
    var seatPrice = new BigDecimal(100);
    var reservationId = UUID.randomUUID().toString();
    var seatNumber = 3;

    var walletAck = await(walletClient.createWallet(walletId, initialBalance),timeoutSec);
    assertEquals(WalletCommandError.NO_ERROR,walletAck.error());
    var showAck = await(showClient.createShow(showId,title,seatPrice,maxSeats),timeoutSec);
    assertEquals(ShowCommandError.NO_ERROR,showAck.error());

     //when
    var reserveSeat = await(showClient.reserveSeat(showId,walletId,reservationId,seatNumber),timeoutSec);
    assertEquals(ShowCommandError.NO_ERROR,reserveSeat.error());

    //then
        Awaitility.await()
        .atMost(10, TimeUnit.of(SECONDS))
              .pollDelay(2, TimeUnit.SECONDS)
              .pollInterval(Duration.ofSeconds(2))
        .ignoreExceptions()
        .untilAsserted(() -> {
          var showSeatStatusGet = await(showClient.getSeatStatus(showId,seatNumber),timeoutSec);
          assertEquals(SeatStatus.PAID,showSeatStatusGet.seatStatus());
          var walletGet = await(walletClient.getWallet(walletId),timeoutSec);
          assertEquals(new BigDecimal(initialBalance).subtract(seatPrice),walletGet.balance());
        });
  }
}