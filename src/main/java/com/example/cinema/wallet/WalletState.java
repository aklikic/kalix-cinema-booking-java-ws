package com.example.cinema.wallet;


import com.example.cinema.util.StateCommandProcessResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.example.cinema.util.StateCommandProcessResult.*;
import static com.example.cinema.wallet.WalletCommand.*;
import static com.example.cinema.wallet.WalletCommandError.*;

public record WalletState(String id, BigDecimal balance, Map<String, Expense> expenses,
                          Set<String> commandIds) {

    public record Expense(String expenseId, BigDecimal amount) {}

    public WalletState(String id, BigDecimal balance) {
        this(id, balance, new HashMap<>(), new HashSet<>());
    }

    private static final String EMPTY_WALLET_ID = "";

    public static WalletState empty() {
        return new WalletState(EMPTY_WALLET_ID, BigDecimal.ZERO, new HashMap<>(), new HashSet<>());
    }

    public StateCommandProcessResult<WalletEvent, WalletCommandError> handleCommand(String walletId, CreateWallet createWallet) {
        if (isEmpty()) {
            return result(new WalletEvent.WalletCreated(walletId, createWallet.initialAmount()));
        } else {
            return error(WALLET_ALREADY_EXISTS);
        }
    }

    public StateCommandProcessResult<WalletEvent, WalletCommandError> handleCommand(ChargeWallet charge) {
        if (isDuplicate(charge)) {
            return error(DUPLICATED_COMMAND);
        } else {
            if (balance.compareTo(charge.amount()) < 0) {
                return resultWithError(new WalletEvent.WalletChargeRejected(id, charge.expenseId(),NOT_SUFFICIENT_FUNDS.name()),NOT_SUFFICIENT_FUNDS);
            } else {
                return result(new WalletEvent.WalletCharged(id, charge.amount(), charge.expenseId()));
            }
        }
    }

    public StateCommandProcessResult<WalletEvent, WalletCommandError> handleCommand(Refund refund) {
        if(expenses.containsKey(refund.chargeExpenseId())){
            Expense expense = expenses.get(refund.chargeExpenseId());
            return result(new WalletEvent.WalletRefunded(id, expense.amount(), expense.expenseId(), refund.commandId()));
        }else{
            return error(EXPENSE_NOT_FOUND);
        }
    }

    public WalletState onEvent(WalletEvent.WalletCreated event){
        return new WalletState(event.walletId(), event.initialAmount(),new HashMap<>(),new HashSet<>());
    }
    public WalletState onEvent(WalletEvent.WalletCharged event){
        Expense expense = new Expense(event.expenseId(), event.amount());
        expenses.put(expense.expenseId(), expense);
        commandIds.add(event.expenseId());
        return new WalletState(id, balance.subtract(event.amount()), expenses, commandIds);
    }
    public WalletState onEvent(WalletEvent.WalletRefunded event){
        expenses.remove(event.chargeExpenseId());
        commandIds.add(event.refundExpenseId());
        return new WalletState(id, balance.add(event.amount()), expenses, commandIds);

    }
    public WalletState onEvent(WalletEvent.WalletChargeRejected event){
        return this;
    }

    public boolean isEmpty() {
        return id.equals(EMPTY_WALLET_ID);
    }

    private boolean isDuplicate(WalletCommand command) {
        if (command instanceof WalletCommand.RequiresDeduplicationCommand c) {
            return commandIds.contains(c.commandId());
        } else {
            return false;
        }
    }
}
