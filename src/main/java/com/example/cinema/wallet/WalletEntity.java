package com.example.cinema.wallet;

import com.example.cinema.util.StateCommandProcessResult;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Id("id")
@TypeId("wallet")
@RequestMapping("/wallet/{id}")
public class WalletEntity extends EventSourcedEntity<WalletState, WalletEvent> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public WalletState emptyState() {
    return WalletState.empty();
  }

  @PostMapping("/create/{initialBalance}")
  public Effect<WalletCommandResponse> createWallet(@PathVariable String id, @PathVariable int initialBalance) {
    var createWallet = new WalletCommand.CreateWallet(BigDecimal.valueOf(initialBalance));
    var result = currentState().handleCommand(id, createWallet);
    return handleStateCommandProcessResult(result);
  }



  @PatchMapping("/charge")
  public Effect<WalletCommandResponse> chargeWallet(@RequestBody WalletCommand.ChargeWallet chargeWallet) {
    if (currentState().isEmpty()) {
      return effects().reply(WalletCommandResponse.of(currentState().id(),WalletCommandError.WALLET_NOT_FOUND));
    }
    var result = currentState().handleCommand(chargeWallet);
    return handleStateCommandProcessResult(result);
  }

  @PatchMapping("/refund/{expenseId}")
  public Effect<WalletCommandResponse> refundWalletCharge(@RequestBody WalletCommand.Refund refund) {
    if (currentState().isEmpty()) {
      return effects().reply(WalletCommandResponse.of(currentState().id(),WalletCommandError.WALLET_NOT_FOUND));
    }
    var result = currentState().handleCommand(refund);
    return handleStateCommandProcessResult(result);
  }

  @GetMapping
  public Effect<WalletCommandResponse> get() {
    if (currentState().isEmpty()) {
      return effects().reply(WalletCommandResponse.of(currentState().id(),WalletCommandError.WALLET_NOT_FOUND));
    } else {
      return effects().reply(WalletCommandResponse.of(currentState()));
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

  private Effect<WalletCommandResponse> handleStateCommandProcessResult(StateCommandProcessResult<WalletEvent,WalletCommandError> result){
    if(!result.events().isEmpty()){
      return effects().emitEvents(result.events())
              .thenReply(updateState ->
                      result.error()
                              .map(error -> WalletCommandResponse.of(updateState.id(), error))
                              .orElse(WalletCommandResponse.of(updateState))
              );
    }else{
      return effects().reply(
              result.error()
                      .map(error -> WalletCommandResponse.of(currentState().id(),error))
                      .orElse(WalletCommandResponse.of(currentState()))
              );
    }
  }
}
