package com.example.cinema.wallet;

import kalix.javasdk.annotations.TypeName;

import java.math.BigDecimal;

sealed public interface WalletPublicEvent {

    @TypeName("pub-wallet-charged")
    record WalletCharged(String walletId, String showId, BigDecimal amount, String expenseId) implements WalletPublicEvent {}

    @TypeName("pub-wallet-refunded")
    record WalletRefunded(String walletId, String showId, BigDecimal amount, String chargeExpenseId, String refundExpenseId) implements WalletPublicEvent {}

    @TypeName("pub-wallet-charge-rejected")
    record WalletChargeRejected(String walletId, String showId, String expenseId, String reason) implements WalletPublicEvent {}
}
