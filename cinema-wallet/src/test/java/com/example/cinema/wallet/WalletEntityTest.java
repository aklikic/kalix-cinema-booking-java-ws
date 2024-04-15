package com.example.cinema.wallet;

import kalix.javasdk.testkit.EventSourcedResult;
import kalix.javasdk.testkit.EventSourcedTestKit;
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
    EventSourcedTestKit<WalletState, WalletEvent, WalletEntity> testKit = EventSourcedTestKit.of(WalletEntity::new);

    //when
    EventSourcedResult<WalletCommandResponse.Ack> result = testKit.call(wallet -> wallet.createWallet(walletId, initialAmount));

    //then
    assertThat(result.isReply()).isTrue();
    assertThat(result.getNextEventOfType(WalletEvent.WalletCreated.class).initialAmount()).isEqualTo(BigDecimal.valueOf(initialAmount));
    assertThat(testKit.getState().id()).isEqualTo(walletId);
    assertThat(testKit.getState().balance()).isEqualTo(BigDecimal.valueOf(initialAmount));
  }

  @Test
  public void shouldChargeWallet() {
    //given
    var walletId = UUID.randomUUID().toString();
    var expenseId = "r1";
    var initialAmount = 100;
    EventSourcedTestKit<WalletState, WalletEvent, WalletEntity> testKit = EventSourcedTestKit.of(WalletEntity::new);
    testKit.call(wallet -> wallet.createWallet(walletId, initialAmount));
    var chargeWallet = new WalletCommand.ChargeWallet(new BigDecimal(10), expenseId);

    //when
    EventSourcedResult<WalletCommandResponse.Ack> result = testKit.call(wallet -> wallet.chargeWallet(chargeWallet));

    //then
    assertThat(result.isReply()).isTrue();
    assertThat(result.getNextEventOfType(WalletEvent.WalletCharged.class)).isEqualTo(new WalletEvent.WalletCharged(walletId, chargeWallet.amount(), expenseId));
    assertThat(testKit.getState().balance()).isEqualTo(new BigDecimal(90));
  }

  @Test
  public void shouldIgnoreChargeDuplicate() {
    //given
    var walletId = UUID.randomUUID().toString();
    var expenseId = "r1";
    var initialAmount = 100;
    EventSourcedTestKit<WalletState, WalletEvent, WalletEntity> testKit = EventSourcedTestKit.of(WalletEntity::new);
    testKit.call(wallet -> wallet.createWallet(walletId, initialAmount));
    var chargeWallet = new WalletCommand.ChargeWallet(new BigDecimal(10), expenseId);
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
    var walletId = UUID.randomUUID().toString();
    var expenseId = "r1";
    var initialAmount = 100;
    EventSourcedTestKit<WalletState, WalletEvent, WalletEntity> testKit = EventSourcedTestKit.of(WalletEntity::new);
    testKit.call(wallet -> wallet.createWallet(walletId, initialAmount));
    var chargeWallet = new WalletCommand.ChargeWallet(new BigDecimal(10), expenseId);
    testKit.call(wallet -> wallet.chargeWallet(chargeWallet));

    //when
    EventSourcedResult<WalletCommandResponse.Ack> result = testKit.call(wallet -> wallet.chargeWallet(chargeWallet));

    //then
    assertThat(result.isReply()).isTrue();
    assertThat(result.didEmitEvents()).isFalse();
    assertThat(testKit.getState().balance()).isEqualTo(new BigDecimal(90));
  }
}