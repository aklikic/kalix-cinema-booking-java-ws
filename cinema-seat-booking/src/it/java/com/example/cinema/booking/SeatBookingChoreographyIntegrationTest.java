package com.example.cinema.booking;


import com.example.cinema.show.SeatStatus;
import com.example.cinema.show.ShowClient;
import com.example.cinema.show.ShowCommandError;
import com.example.cinema.wallet.WalletClient;
import com.example.cinema.wallet.WalletCommandError;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import kalix.spring.testkit.KalixIntegrationTestKitSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;


//@SpringBootTest(classes = Main.class)
public class SeatBookingChoreographyIntegrationTest {


  private  WalletClient walletClient;
  private  ShowClient showClient;

    public SeatBookingChoreographyIntegrationTest() throws Exception{
        var config = ConfigFactory.load();
        this.walletClient = new WalletClient(getWebClient(config,"cinema-wallet"));
        this.showClient = new ShowClient(getWebClient(config,"cinema-show"));
    }
    private static final long timeoutSec = 10;

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

    var walletAck = walletClient.createWallet(walletId, initialBalance).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
    assertEquals(WalletCommandError.NO_ERROR,walletAck.error());
    var showAck = showClient.createShow(showId,title,seatPrice,maxSeats).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
    assertEquals(ShowCommandError.NO_ERROR,showAck.error());

     //when
    var reserveSeat = showClient.reserveSeat(showId,walletId,reservationId,seatNumber).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
    assertEquals(ShowCommandError.NO_ERROR,reserveSeat.error());

    //then
      await()
        .atMost(10, TimeUnit.of(SECONDS))
              .pollDelay(2, TimeUnit.SECONDS)
              .pollInterval(Duration.ofSeconds(2))
        .ignoreExceptions()
        .untilAsserted(() -> {
          var showSeatStatusGet = showClient.getSeatStatus(showId,seatNumber).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
          assertEquals(SeatStatus.PAID,showSeatStatusGet.seatStatus());
          var walletGet = walletClient.getWallet(walletId).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
          assertEquals(new BigDecimal(initialBalance).subtract(seatPrice),walletGet.balance());
        });
  }
    private WebClient getWebClient(Config config, String serviceName){
        var mapping = config.getString("kalix.dev-mode.service-port-mappings."+serviceName);
        return WebClient.create("http://" + mapping);
    }

}