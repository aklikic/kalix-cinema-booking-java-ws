package com.example.cinema.booking;

import com.example.cinema.wallet.WalletCommand;
import com.example.cinema.wallet.WalletCommandResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

@Component
public class WalletClient {

    @Autowired
    private WebClient webClient;

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
