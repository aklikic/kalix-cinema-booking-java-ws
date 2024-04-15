package com.example.cinema;

import com.example.cinema.show.Main;
import com.example.cinema.show.ShowClient;
import com.example.cinema.show.ShowCommandError;
import kalix.spring.testkit.KalixIntegrationTestKitSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;


/**
 * This is a skeleton for implementing integration tests for a Kalix application built with the Java SDK.
 *
 * This test will initiate a Kalix Runtime using testcontainers and therefore it's required to have Docker installed
 * on your machine. This test will also start your Spring Boot application.
 *
 * Since this is an integration tests, it interacts with the application using a WebClient
 * (already configured and provided automatically through injection).
 */
@SpringBootTest(classes = Main.class)
public class IntegrationTest extends KalixIntegrationTestKitSupport {

  private static final long timeoutSec = 10;
  private final ShowClient showClient;

  public IntegrationTest(@Autowired WebClient webClient) {
    this.showClient = new ShowClient(webClient);
  }

  @Test
  public void test() throws Exception {
    //given
    var walletId = UUID.randomUUID().toString();
    var showId = UUID.randomUUID().toString();
    var title = "title";
    var maxSeats = 10 ;
    var seatPrice = new BigDecimal(100);
    var reservationId = UUID.randomUUID().toString();
    var seatNumber = 3;

    var showAck = showClient.createShow(showId,title,seatPrice,maxSeats).toCompletableFuture().get(timeoutSec, TimeUnit.SECONDS);
    assertEquals(ShowCommandError.NO_ERROR,showAck.error());

    var reserveAck = showClient.reserveSeat(showId,walletId,reservationId,seatNumber).toCompletableFuture().get(timeoutSec, TimeUnit.SECONDS);
    assertEquals(ShowCommandError.NO_ERROR,reserveAck.error());
    assertEquals(seatPrice,reserveAck.price());

    var cancelAck = showClient.cancelSeatReservation(showId,reservationId).toCompletableFuture().get(timeoutSec, TimeUnit.SECONDS);
    assertEquals(ShowCommandError.NO_ERROR,cancelAck.error());
  }
}