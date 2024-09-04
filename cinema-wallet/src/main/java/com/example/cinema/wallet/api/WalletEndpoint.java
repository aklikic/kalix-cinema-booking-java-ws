package com.example.cinema.wallet.api;

import akka.javasdk.annotations.http.Endpoint;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.Patch;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.example.cinema.wallet.WalletCommand;
import com.example.cinema.wallet.WalletCommandResponse;
import com.example.cinema.wallet.application.WalletEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

@Endpoint("/wallet")
public class WalletEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(WalletEndpoint.class);

    private final ComponentClient componentClient;

    public WalletEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Post("/{id}/create/{initialBalance}")
    public CompletionStage<WalletCommandResponse.Ack> createWallet(String id, int initialBalance) {
        logger.info("createWallet: id=[{}], initialBalance=[{}]", id, initialBalance);
        return componentClient.forEventSourcedEntity(id).method(WalletEntity::createWallet).invokeAsync(initialBalance);
    }



    @Patch("/{id}/charge")
    public CompletionStage<WalletCommandResponse.Ack> chargeWallet(String id, WalletCommand.ChargeWallet chargeWallet) {
        logger.info("chargeWallet: walletId=[{}], chargeWallet=[{}]", id, chargeWallet);
        return componentClient.forEventSourcedEntity(id).method(WalletEntity::chargeWallet).invokeAsync(chargeWallet);
    }

    @Patch("/{id}/refund")
    public CompletionStage<WalletCommandResponse.Ack> refundWalletCharge(String id, WalletCommand.Refund refund) {
        logger.info("refundWalletCharge: walletId=[{}], refund=[{}]", id, refund);
        return componentClient.forEventSourcedEntity(id).method(WalletEntity::refundWalletCharge).invokeAsync(refund);
    }

    @Get("/{id}")
    public CompletionStage<WalletCommandResponse.WalletCommandSummeryResponse> get(String id) {
        logger.info("get: walletId=[{}]", id);
        return componentClient.forEventSourcedEntity(id).method(WalletEntity::get).invokeAsync();
    }
}
