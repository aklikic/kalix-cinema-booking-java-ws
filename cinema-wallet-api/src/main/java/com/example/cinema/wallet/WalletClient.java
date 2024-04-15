package com.example.cinema.wallet;

import kalix.spring.WebClientProvider;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

public class WalletClient {

    final private WebClient webClient;

    public WalletClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public CompletionStage<WalletCommandResponse.Ack> createWallet(String walletId, int initialBalance) {
        return webClient.post().uri("/wallet/" + walletId + "/create/" + initialBalance)
                .retrieve()
                .bodyToMono(WalletCommandResponse.Ack.class)
                .toFuture();
    }


    public CompletionStage<WalletCommandResponse.Ack> chargeWallet(String walletId, BigDecimal amount, String expenseId) {
        return webClient.patch().uri("/wallet/" + walletId + "/charge")
                .bodyValue(new WalletCommand.ChargeWallet(amount,expenseId))
                .retrieve()
                .bodyToMono(WalletCommandResponse.Ack.class)
                .toFuture();
    }

    public CompletionStage<WalletCommandResponse.Ack> refundWalletCharge(String walletId, String chargeExpenseId, String refundExpenseId) {
        return webClient.patch().uri("/wallet/" + walletId + "/refund")
                .bodyValue(new WalletCommand.Refund(chargeExpenseId,refundExpenseId))
                .retrieve()
                .bodyToMono(WalletCommandResponse.Ack.class)
                .toFuture();
    }

    public CompletionStage<WalletCommandResponse.WalletCommandSummeryResponse> getWallet(String walletId) {
        return webClient.get().uri("/wallet/" + walletId)
                .retrieve()
                .bodyToMono(WalletCommandResponse.WalletCommandSummeryResponse.class)
                .toFuture();
    }
}