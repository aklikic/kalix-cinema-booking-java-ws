package com.example.cinema.wallet.application;

import akka.javasdk.testkit.EventSourcedResult;
import akka.javasdk.testkit.EventSourcedTestKit;
import com.example.cinema.wallet.WalletCommand;
import com.example.cinema.wallet.WalletCommandResponse;
import com.example.cinema.wallet.domain.WalletEvent;
import com.example.cinema.wallet.domain.WalletState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WalletEntityTest {


  @Test
  public void shouldCreateWallet() {
    //given
    var walletId = UUID.randomUUID().toString();
    var initialAmount = 100;
    EventSourcedTestKit<WalletState, WalletEvent, WalletEntity> testKit = EventSourcedTestKit.of(walletId,WalletEntity::new);

    //when
    EventSourcedResult<WalletCommandResponse.Ack> result = testKit.call(wallet -> wallet.createWallet(initialAmount));

    //then
    assertThat(result.isReply()).isTrue();
    assertThat(result.getNextEventOfType(WalletEvent.WalletCreated.class).initialAmount()).isEqualTo(BigDecimal.valueOf(initialAmount));
    assertThat(testKit.getState().id()).isEqualTo(walletId);
    assertThat(testKit.getState().balance()).isEqualTo(BigDecimal.valueOf(initialAmount));
  }

  @Test
  public void shouldChargeWallet() {
    //given
    var showId = UUID.randomUUID().toString();
    var walletId = UUID.randomUUID().toString();
    var expenseId = "r1";
    var initialAmount = 100;
    EventSourcedTestKit<WalletState, WalletEvent, WalletEntity> testKit = EventSourcedTestKit.of(walletId, WalletEntity::new);
    testKit.call(wallet -> wallet.createWallet(initialAmount));
    var chargeWallet = new WalletCommand.ChargeWallet(new BigDecimal(10), expenseId, showId);

    //when
    EventSourcedResult<WalletCommandResponse.Ack> result = testKit.call(wallet -> wallet.chargeWallet(chargeWallet));

    //then
    assertThat(result.isReply()).isTrue();
    assertThat(result.getNextEventOfType(WalletEvent.WalletCharged.class)).isEqualTo(new WalletEvent.WalletCharged(walletId, showId, chargeWallet.amount(), expenseId));
    assertThat(testKit.getState().balance()).isEqualTo(new BigDecimal(90));
  }

  @Test
  public void shouldIgnoreChargeDuplicate() {
    //given
    var showId = UUID.randomUUID().toString();
    var walletId = UUID.randomUUID().toString();
    var expenseId = "r1";
    var initialAmount = 100;
    EventSourcedTestKit<WalletState, WalletEvent, WalletEntity> testKit = EventSourcedTestKit.of(walletId,WalletEntity::new);
    testKit.call(wallet -> wallet.createWallet(initialAmount));
    var chargeWallet = new WalletCommand.ChargeWallet(new BigDecimal(10), expenseId, showId);
    testKit.call(wallet -> wallet.chargeWallet(chargeWallet));

    //when
    EventSourcedResult<WalletCommandResponse.Ack> result = testKit.call(wallet -> wallet.chargeWallet(chargeWallet));

    //then
    assertThat(result.isReply()).isTrue();
    assertThat(result.didEmitEvents()).isFalse();
    assertThat(testKit.getState().balance()).isEqualTo(new BigDecimal(90));
  }

  @Test
  public void shouldRefundWallet() {
    //given
    var showId = UUID.randomUUID().toString();
    var walletId = UUID.randomUUID().toString();
    var expenseId = "r1";
    var initialAmount = 100;
    EventSourcedTestKit<WalletState, WalletEvent, WalletEntity> testKit = EventSourcedTestKit.of(walletId, WalletEntity::new);
    testKit.call(wallet -> wallet.createWallet(initialAmount));
    var chargeWallet = new WalletCommand.ChargeWallet(new BigDecimal(10), expenseId, showId);
    testKit.call(wallet -> wallet.chargeWallet(chargeWallet));

    //when
    EventSourcedResult<WalletCommandResponse.Ack> result = testKit.call(wallet -> wallet.chargeWallet(chargeWallet));

    //then
    assertThat(result.isReply()).isTrue();
    assertThat(result.didEmitEvents()).isFalse();
    assertThat(testKit.getState().balance()).isEqualTo(new BigDecimal(90));
  }
}