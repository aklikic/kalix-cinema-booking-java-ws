package com.example.cinema.booking;


import akka.javasdk.http.HttpClient;
import akka.javasdk.http.HttpClientProvider;
import akka.javasdk.testkit.TestKitSupport;
import com.example.cinema.booking.api.SeatBookingCommandError;
import com.example.cinema.booking.domain.SeatBookingState;
import com.example.cinema.show.SeatStatus;
import com.example.cinema.show.ShowClient;
import com.example.cinema.show.ShowCommandError;
import com.example.cinema.wallet.WalletClient;
import com.example.cinema.wallet.WalletCommandError;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SeatBookingIntegrationTest extends TestKitSupport {

  private static final Logger logger = LoggerFactory.getLogger(SeatBookingIntegrationTest.class);
  private SeatBookingClient seatBookingClient;

  private  WalletClient walletClient;
  private  ShowClient showClient;

  private static final long timeoutSec = 10;

  public void beforeAll() {
        super.beforeAll();
        seatBookingClient = new SeatBookingClient(componentClient);
        logger.info("httpClient show: {}",testKit.getHttpClientProvider().httpClientFor("cinema-show"));
        logger.info("httpClient wallet: {}",testKit.getHttpClientProvider().httpClientFor("cinema-wallet"));
        this.walletClient = new WalletClient(testKit.getHttpClientProvider().httpClientFor("cinema-wallet"));
        this.showClient = new ShowClient(testKit.getHttpClientProvider().httpClientFor("cinema-show"));
  }
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
    var bookSeatRes = seatBookingClient.start(reservationId,showId,seatNumber,walletId).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
    assertEquals(SeatBookingCommandError.NO_ERROR,bookSeatRes.error());

    //then
      Awaitility.await()
        .atMost(10, TimeUnit.of(SECONDS))
        .ignoreExceptions()
        .untilAsserted(() -> {
          var getSeatBooking = seatBookingClient.get(reservationId).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
          assertTrue(getSeatBooking.state().isPresent());
          assertEquals(SeatBookingState.SeatBookingStateStatus.COMPLETED,getSeatBooking.state().get().status());

          var walletGet = walletClient.getWallet(walletId).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
          assertEquals(new BigDecimal(initialBalance).subtract(seatPrice),walletGet.balance());

          var showSeatStatusGet = showClient.getSeatStatus(showId,seatNumber).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
          assertEquals(SeatStatus.PAID,showSeatStatusGet.seatStatus());
        });
  }
    @Test
    public void shouldRejectReservationIfCaseOfInsufficientWalletBalance() throws Exception{
        //given
        var walletId = UUID.randomUUID().toString();
        var initialBalance = 50;//insufficient balance
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
        var bookSeatRes = seatBookingClient.start(reservationId,showId,seatNumber,walletId).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
        assertEquals(SeatBookingCommandError.NO_ERROR,bookSeatRes.error());

        //then
        Awaitility.await()
                .atMost(10, TimeUnit.of(SECONDS))
                .ignoreExceptions()
                .untilAsserted(() -> {
                    var getSeatBooking = seatBookingClient.get(reservationId).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
                    assertTrue(getSeatBooking.state().isPresent());
                    assertEquals(SeatBookingState.SeatBookingStateStatus.WALLET_CHARGE_REJECTED,getSeatBooking.state().get().status());
                    assertEquals(WalletCommandError.NOT_SUFFICIENT_FUNDS.name(),getSeatBooking.state().get().failReason().get());

                    var walletGet = walletClient.getWallet(walletId).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
                    assertEquals(new BigDecimal(initialBalance),walletGet.balance());

                    var showSeatStatusGet = showClient.getSeatStatus(showId,seatNumber).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
                    assertEquals(SeatStatus.AVAILABLE,showSeatStatusGet.seatStatus());
                });
    }


}