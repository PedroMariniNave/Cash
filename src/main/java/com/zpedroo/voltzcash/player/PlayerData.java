package com.zpedroo.voltzcash.player;

import com.zpedroo.voltzcash.purchases.Purchase;
import com.zpedroo.voltzcash.transactions.Transaction;
import com.zpedroo.voltzcash.transactions.TransactionType;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private UUID uuid;
    private BigInteger cash;
    private List<Transaction> transactions;
    private List<Purchase> purchases;
    private Boolean update;

    public PlayerData(UUID uuid, BigInteger cash, List<Transaction> transactions, List<Purchase> purchases) {
        this.uuid = uuid;
        this.cash = cash;
        this.transactions = transactions == null ? new LinkedList<>() : transactions;
        this.purchases = purchases == null ? new LinkedList<>() : purchases;
        this.update = false;
    }

    public UUID getUUID() {
        return uuid;
    }

    public BigInteger getCash() {
        return cash;
    }

    public void addCash(BigInteger amount) {
        if (amount.signum() < 0) return;

        this.cash = cash.add(amount);
        this.setQueue(true);
    }

    public void removeCash(BigInteger amount) {
        if (amount.signum() < 0) return;

        this.cash = cash.subtract(amount);
        this.setQueue(true);

        if (cash.signum() < 0) cash = BigInteger.ZERO;
    }

    public void setCash(BigInteger amount) {
        if (amount.signum() < 0) return;

        this.cash = amount;
        this.setQueue(true);

        if (cash.signum() < 0) cash = BigInteger.ZERO;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public Boolean isQueueUpdate() {
        return update;
    }

    public void setQueue(Boolean status) {
        this.update = status;
    }

    public void addTransaction(OfflinePlayer actor, OfflinePlayer target, BigInteger amount, TransactionType type, Integer id) {
        if (transactions.size() == 225) transactions.remove(0);

        transactions.add(new Transaction(actor, target, amount, type, new Date(), id));
    }

    public void addPurchase(String name, Integer amount, BigInteger price, Integer id) {
        if (purchases.size() == 225) purchases.remove(0);

        purchases.add(new Purchase(name, amount, price, new Date(), id));
    }
}