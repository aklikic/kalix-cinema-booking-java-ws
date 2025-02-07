package com.example.cinema.wallet;


import akka.javasdk.http.HttpClient;
import akka.javasdk.http.StrictResponse;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

public class WalletClient {

    final private HttpClient httpClient;

    public WalletClient(HttpClient webClient) {
        this.httpClient = webClient;
    }

    public CompletionStage<WalletCommandResponse.Ack> createWallet(String walletId, int initialBalance) {
        return httpClient.POST("/wallet/" + walletId + "/create/" + initialBalance)
                .responseBodyAs(WalletCommandResponse.Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }


    public CompletionStage<WalletCommandResponse.Ack> chargeWallet(String walletId, BigDecimal amount, String expenseId, String showId) {
        return httpClient.PATCH("/wallet/" + walletId + "/charge")
                .withRequestBody(new WalletCommand.ChargeWallet(amount,expenseId,showId))
                .responseBodyAs(WalletCommandResponse.Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }

    public CompletionStage<WalletCommandResponse.Ack> refundWalletCharge(String walletId, String chargeExpenseId, String refundExpenseId) {
        return httpClient.PATCH("/wallet/" + walletId + "/refund")
                .withRequestBody(new WalletCommand.Refund(chargeExpenseId,refundExpenseId))
                .responseBodyAs(WalletCommandResponse.Ack.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }

    public CompletionStage<WalletCommandResponse.WalletCommandSummeryResponse> getWallet(String walletId) {
        return httpClient.GET("/wallet/" + walletId)
                .responseBodyAs(WalletCommandResponse.WalletCommandSummeryResponse.class)
                .invokeAsync()
                .thenApply(StrictResponse::body);
    }
}