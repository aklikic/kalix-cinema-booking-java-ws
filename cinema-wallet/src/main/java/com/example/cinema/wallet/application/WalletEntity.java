package com.example.cinema.wallet.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.example.cinema.util.StateCommandProcessResult;
import com.example.cinema.wallet.*;
import com.example.cinema.wallet.domain.WalletEvent;
import com.example.cinema.wallet.domain.WalletState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@ComponentId("wallet")
public class WalletEntity extends EventSourcedEntity<WalletState, WalletEvent> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final String walletId;

  public WalletEntity(EventSourcedEntityContext context) {
    this.walletId = context.entityId();
  }

  @Override
  public WalletState emptyState() {
    return WalletState.empty();
  }

  public Effect<WalletCommandResponse.Ack> createWallet(int initialBalance) {
    logger.info("createWallet: id=[{}], initialBalance=[{}]", walletId, initialBalance);
    var createWallet = new WalletCommand.CreateWallet(BigDecimal.valueOf(initialBalance));
    var result = currentState().handleCommand(walletId, createWallet);
    return handleStateCommandProcessResult(result);
  }

  public Effect<WalletCommandResponse.Ack> chargeWallet(WalletCommand.ChargeWallet chargeWallet) {
    logger.info("chargeWallet: walletId=[{}], chargeWallet=[{}]", chargeWallet);
    if (currentState().isEmpty()) {
      return effects().reply(WalletCommandResponse.Ack.error(WalletCommandError.WALLET_NOT_FOUND));
    }
    var result = currentState().handleCommand(chargeWallet);
    return handleStateCommandProcessResult(result);
  }

  public Effect<WalletCommandResponse.Ack> refundWalletCharge(WalletCommand.Refund refund) {
    logger.info("refundWalletCharge: walletId=[{}], refund=[{}]", walletId, refund);
    if (currentState().isEmpty()) {
      return effects().reply(WalletCommandResponse.Ack.error(WalletCommandError.WALLET_NOT_FOUND));
    }
    var result = currentState().handleCommand(refund);
    return handleStateCommandProcessResult(result);
  }

  public ReadOnlyEffect<WalletCommandResponse.WalletCommandSummeryResponse> get() {
    logger.info("get: walletId=[{}]", walletId);
    if (currentState().isEmpty()) {
      return effects().reply(WalletCommandResponse.WalletCommandSummeryResponse.error(currentState().id(),WalletCommandError.WALLET_NOT_FOUND));
    } else {
      return effects().reply(WalletCommandResponse.WalletCommandSummeryResponse.ok(currentState().id(),currentState().balance()));
    }
  }


  @Override
  public WalletState applyEvent(WalletEvent walletEvent) {
    return switch (walletEvent){
      case WalletEvent.WalletCreated evt -> currentState().onEvent(evt);
      case WalletEvent.WalletCharged evt -> currentState().onEvent(evt);
      case WalletEvent.WalletRefunded evt -> currentState().onEvent(evt);
      case WalletEvent.WalletChargeRejected evt -> currentState().onEvent(evt);
    };
  }


  private Effect<WalletCommandResponse.Ack> handleStateCommandProcessResult(StateCommandProcessResult<WalletEvent,WalletCommandError> result){
    if(!result.events().isEmpty()){
      return effects().persistAll(result.events())
              .thenReply(updateState ->
                      result.error()
                              .map(error -> WalletCommandResponse.Ack.error( error))
                              .orElse(WalletCommandResponse.Ack.ok())
              );
    }else{
      return effects().reply(
              result.error()
                      .map(error -> WalletCommandResponse.Ack.error(error))
                      .orElse(WalletCommandResponse.Ack.ok())
              );
    }
  }
}
