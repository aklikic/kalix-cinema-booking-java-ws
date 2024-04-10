package com.example.cinema.wallet;

import java.math.BigDecimal;

record WalletCommandResponse(String id, BigDecimal balance, WalletCommandError error) {
    public static WalletCommandResponse of(WalletState wallet) {
        return new WalletCommandResponse(wallet.id(), wallet.balance(), WalletCommandError.NO_ERROR);
    }
    public static WalletCommandResponse of(String id, WalletCommandError error) {
        return new WalletCommandResponse(id, BigDecimal.ZERO, error);
    }

}
