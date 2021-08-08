package com.zpedroo.voltzcash.purchases;

import java.math.BigInteger;
import java.util.Date;

public class Purchase {

    private String name;
    private Integer amount;
    private BigInteger price;
    private Date date;
    private Integer id;

    public Purchase(String name, Integer amount, BigInteger price, Date date, Integer id) {
        this.name = name;
        this.amount = amount;
        this.price = price;
        this.date = date;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Integer getAmount() {
        return amount;
    }

    public BigInteger getPrice() {
        return price;
    }

    public Date getDate() {
        return date;
    }

    public Integer getID() {
        return id;
    }
}
