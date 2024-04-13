package com.example.cinema.wallet;

import java.math.BigDecimal;

public interface WalletCommandResponse{

    WalletCommandError error();

    record Ack(WalletCommandError error) implements WalletCommandResponse {
        public static Ack ok() {
            return new Ack(WalletCommandError.NO_ERROR);
        }
        public static Ack error(WalletCommandError error) {
            return new Ack(error);
        }

    }
    record WalletCommandSummeryResponse(String id, BigDecimal balance, WalletCommandError error) implements WalletCommandResponse {
        public static WalletCommandSummeryResponse ok(String walletId, BigDecimal balance) {
            return new WalletCommandSummeryResponse(walletId, balance, WalletCommandError.NO_ERROR);
        }
        public static WalletCommandSummeryResponse error(String id, WalletCommandError error) {
            return new WalletCommandSummeryResponse(id, BigDecimal.ZERO, error);
        }

    }
}
