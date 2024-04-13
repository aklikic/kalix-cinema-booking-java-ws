package com.example.cinema.booking;

import com.example.cinema.client.WalletClient;
import com.example.cinema.wallet.WalletCommand;
import com.example.cinema.wallet.WalletCommandResponse;
import com.example.cinema.wallet.WalletEntity;
import kalix.javasdk.client.ComponentClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

@Service
public class WalletClientComponentClient implements WalletClient {

    private final ComponentClient componentClient;

    public WalletClientComponentClient(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Override
    public CompletionStage<WalletCommandResponse.Ack> createWallet(String walletId, int initialBalance) {
        return componentClient.forEventSourcedEntity(walletId)
                .call(WalletEntity::createWallet)
                .params(walletId,initialBalance)
                .execute();
    }

    @Override
    public CompletionStage<WalletCommandResponse.Ack> chargeWallet(String walletId, BigDecimal amount, String expenseId) {
        return componentClient.forEventSourcedEntity(walletId)
                .call(WalletEntity::chargeWallet)
                .params(new WalletCommand.ChargeWallet(amount, expenseId))
                .execute();
    }

    @Override
    public CompletionStage<WalletCommandResponse.Ack> refundWalletCharge(String walletId, String chargeExpenseId, String refundExpenseId) {
        return componentClient.forEventSourcedEntity(walletId)
                .call(WalletEntity::refundWalletCharge)
                .params(new WalletCommand.Refund(chargeExpenseId, refundExpenseId))
                .execute();
    }

    @Override
    public CompletionStage<WalletCommandResponse.WalletCommandSummeryResponse> getWallet(String walletId) {
        return componentClient.forEventSourcedEntity(walletId)
                .call(WalletEntity::get)
                .execute();
    }
}
