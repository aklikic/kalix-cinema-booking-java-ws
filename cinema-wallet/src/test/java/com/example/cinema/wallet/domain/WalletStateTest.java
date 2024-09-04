package com.example.cinema.wallet.domain;

import com.example.cinema.wallet.WalletCommand;
import com.example.cinema.wallet.WalletCommandError;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WalletStateTest {

  @Test
  public void shouldCreateWallet() {
    //given
    var wallet = WalletState.empty();
    var walletId = "1";
    var createWallet = new WalletCommand.CreateWallet(BigDecimal.TEN);

    //when
    var result = wallet.handleCommand(walletId, createWallet);
    assertTrue(result.error().isEmpty());
    assertEquals(1, result.events().size());
    var event = result.events().get(0);
    var updatedWallet = wallet.onEvent((WalletEvent.WalletCreated) event);

    //then
    assertEquals(walletId, updatedWallet.id());
    assertEquals(createWallet.initialAmount(), updatedWallet.balance());
  }

  @Test
  public void shouldRejectCommandIfWalletExists() {
    //given
    var walletId = "1";
    var wallet = new WalletState(walletId, BigDecimal.TEN);
    var createWallet = new WalletCommand.CreateWallet(BigDecimal.TEN);

    //when
    var result = wallet.handleCommand(walletId, createWallet);

    //then
    assertEquals(WalletCommandError.WALLET_ALREADY_EXISTS,result.error().orElse(WalletCommandError.NO_ERROR));
  }

  @Test
  public void shouldChargeWallet() {
    //given
    var wallet = new WalletState("1", BigDecimal.TEN);
    var chargeWallet = new WalletCommand.ChargeWallet(BigDecimal.valueOf(3), UUID.randomUUID().toString(), UUID.randomUUID().toString());

    //when
    var result = wallet.handleCommand(chargeWallet);
    assertTrue(result.error().isEmpty());
    assertEquals(1, result.events().size());
    var event = result.events().get(0);
    var updatedWallet = wallet.onEvent((WalletEvent.WalletCharged) event);

    //then
    assertEquals(BigDecimal.valueOf(7), updatedWallet.balance());
  }

  @Test
  public void shouldRejectDuplicatedCharge() {
    //given
    var wallet = new WalletState("1", BigDecimal.TEN);
    var chargeWallet = new WalletCommand.ChargeWallet(BigDecimal.valueOf(3), UUID.randomUUID().toString(), UUID.randomUUID().toString());

    var result = wallet.handleCommand(chargeWallet);
    assertTrue(result.error().isEmpty());
    assertEquals(1, result.events().size());
    var event = result.events().get(0);
    var updatedWallet = wallet.onEvent((WalletEvent.WalletCharged) event);

    //when
    result = updatedWallet.handleCommand(chargeWallet);

    //then
    assertEquals(WalletCommandError.DUPLICATED_COMMAND,result.error().orElse(WalletCommandError.NO_ERROR));
  }
}