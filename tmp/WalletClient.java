package com.example.cinema.client;

import com.example.cinema.wallet.WalletCommandResponse;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

public interface WalletClient {


    CompletionStage<WalletCommandResponse.Ack> createWallet(String walletId, int initialBalance);

    CompletionStage<WalletCommandResponse.Ack> chargeWallet(String walletId, BigDecimal amount, String expenseId);

    CompletionStage<WalletCommandResponse.Ack> refundWalletCharge(String walletId, String chargeExpenseId, String refundExpenseId) ;

    CompletionStage<WalletCommandResponse.WalletCommandSummeryResponse> getWallet(String walletId);
}