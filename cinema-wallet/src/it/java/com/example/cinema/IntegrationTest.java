package com.example.cinema;

import akka.javasdk.testkit.AkkaSdkTestKitSupport;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class IntegrationTest extends AkkaSdkTestKitSupport {

  private Duration timeout = Duration.of(5, SECONDS);

  @Test
  public void test() throws Exception {
  }
}