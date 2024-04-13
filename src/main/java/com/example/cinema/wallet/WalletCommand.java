package com.example.cinema.wallet;

import java.math.BigDecimal;

public sealed interface WalletCommand {

    sealed interface RequiresDeduplicationCommand extends WalletCommand {
        String commandId();
    }

    record CreateWallet(BigDecimal initialAmount) implements WalletCommand {
    }

    record ChargeWallet(BigDecimal amount, String expenseId) implements RequiresDeduplicationCommand {
        @Override
        public String commandId() {
            return expenseId;
        }
    }

    record Refund(String chargeExpenseId, String refundExpenseId) implements RequiresDeduplicationCommand {
        @Override
        public String commandId() {
            return refundExpenseId;
        }
    }


}
