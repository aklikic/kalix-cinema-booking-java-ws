package com.example.cinema.wallet;

import com.example.cinema.util.StateCommandProcessResult;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Id("id")
@TypeId("wallet")
@RequestMapping("/wallet/{id}")
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

  @PostMapping("/create/{initialBalance}")
  public Effect<WalletCommandResponse.Ack> createWallet(@PathVariable String id, @PathVariable int initialBalance) {
    logger.info("createWallet: id=[{}], initialBalance=[{}]", walletId, initialBalance);
    var createWallet = new WalletCommand.CreateWallet(BigDecimal.valueOf(initialBalance));
    var result = currentState().handleCommand(id, createWallet);
    return handleStateCommandProcessResult(result);
  }



  @PatchMapping("/charge")
  public Effect<WalletCommandResponse.Ack> chargeWallet(@RequestBody WalletCommand.ChargeWallet chargeWallet) {
    logger.info("chargeWallet: walletId=[{}], chargeWallet=[{}]", chargeWallet);
    if (currentState().isEmpty()) {
      return effects().reply(WalletCommandResponse.Ack.error(WalletCommandError.WALLET_NOT_FOUND));
    }
    var result = currentState().handleCommand(chargeWallet);
    return handleStateCommandProcessResult(result);
  }

  @PatchMapping("/refund")
  public Effect<WalletCommandResponse.Ack> refundWalletCharge(@RequestBody WalletCommand.Refund refund) {
    logger.info("refundWalletCharge: walletId=[{}], refund=[{}]", walletId, refund);
    if (currentState().isEmpty()) {
      return effects().reply(WalletCommandResponse.Ack.error(WalletCommandError.WALLET_NOT_FOUND));
    }
    var result = currentState().handleCommand(refund);
    return handleStateCommandProcessResult(result);
  }

  @GetMapping
  public Effect<WalletCommandResponse.WalletCommandSummeryResponse> get() {
    logger.info("get: walletId=[{}]", walletId);
    if (currentState().isEmpty()) {
      return effects().reply(WalletCommandResponse.WalletCommandSummeryResponse.error(currentState().id(),WalletCommandError.WALLET_NOT_FOUND));
    } else {
      return effects().reply(WalletCommandResponse.WalletCommandSummeryResponse.ok(currentState().id(),currentState().balance()));
    }
  }


  @EventHandler
  public WalletState onEvent(WalletEvent.WalletCreated walletCreated) {
    return currentState().onEvent(walletCreated);
  }

  @EventHandler
  public WalletState onEvent(WalletEvent.WalletCharged walletCharged) {
    return currentState().onEvent(walletCharged);
  }

  @EventHandler
  public WalletState onEvent(WalletEvent.WalletRefunded walletRefunded) {
    return currentState().onEvent(walletRefunded);
  }

  @EventHandler
  public WalletState onEvent(WalletEvent.WalletChargeRejected walletCharged) {
    return currentState().onEvent(walletCharged);
  }

  private Effect<WalletCommandResponse.Ack> handleStateCommandProcessResult(StateCommandProcessResult<WalletEvent,WalletCommandError> result){
    if(!result.events().isEmpty()){
      return effects().emitEvents(result.events())
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
