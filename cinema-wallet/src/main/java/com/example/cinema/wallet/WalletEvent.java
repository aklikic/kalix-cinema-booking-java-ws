package com.example.cinema.wallet;

import kalix.javasdk.annotations.TypeName;

import java.math.BigDecimal;

sealed public interface WalletEvent {

    @TypeName("wallet-created")
    record WalletCreated(String walletId, BigDecimal initialAmount) implements WalletEvent {}

    @TypeName("wallet-charged")
    record WalletCharged(String walletId, String showId, BigDecimal amount, String expenseId) implements WalletEvent {}

    @TypeName("wallet-refunded")
    record WalletRefunded(String walletId, String showId, BigDecimal amount, String chargeExpenseId, String refundExpenseId) implements WalletEvent {}

    @TypeName("wallet-charge-rejected")
    record WalletChargeRejected(String walletId, String showId, String expenseId, String reason) implements WalletEvent {}
}
