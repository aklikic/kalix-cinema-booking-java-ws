package com.example.cinema.booking;

import com.example.cinema.Main;
import com.example.cinema.show.ShowCommandError;
import com.example.cinema.show.ShowState;
import com.example.cinema.wallet.WalletCommandError;
import kalix.spring.testkit.KalixIntegrationTestKitSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;



@SpringBootTest(classes = Main.class)
public class SeatBookingIntegrationTest extends KalixIntegrationTestKitSupport {

  @Autowired
  private ShowClient showClient;

  @Autowired
  private WalletClient walletClient;

  @Autowired
  private SeatBookingClient seatBookingClient;


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
    var bookSeatRes = seatBookingClient.start(reservationId,showId,seatNumber,walletId).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
    assertEquals(SeatBookingCommandError.NO_ERROR,bookSeatRes.error());

    //then
      await()
        .atMost(10, TimeUnit.of(SECONDS))
        .ignoreExceptions()
        .untilAsserted(() -> {
          var getSeatBooking = seatBookingClient.get(reservationId).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
          assertTrue(getSeatBooking.state().isPresent());
          assertEquals(SeatBookingState.SeatBookingStateStatus.COMPLETED,getSeatBooking.state().get().status());

          var walletGet = walletClient.getWallet(walletId).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
          assertEquals(new BigDecimal(initialBalance).subtract(seatPrice),walletGet.balance());

          var showSeatStatusGet = showClient.getSeatStatus(showId,seatNumber).toCompletableFuture().get(timeoutSec,TimeUnit.SECONDS);
          assertEquals(ShowState.SeatStatus.PAID,showSeatStatusGet.seatStatus());
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
        await()
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
                    assertEquals(ShowState.SeatStatus.AVAILABLE,showSeatStatusGet.seatStatus());
                });
    }
}