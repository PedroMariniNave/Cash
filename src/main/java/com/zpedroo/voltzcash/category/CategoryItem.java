package com.zpedroo.voltzcash.category;

import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.List;

public class CategoryItem {

    private Integer slot;
    private BigInteger price;
    private ItemStack display;
    private ItemStack shopItem;
    private List<String> commands;
    private Boolean selectAmount;

    public CategoryItem(Integer slot, BigInteger price, ItemStack display, ItemStack shopItem, List<String> commands, Boolean selectAmount) {
        this.slot = slot;
        this.price = price;
        this.display = display;
        this.shopItem = shopItem;
        this.commands = commands;
        this.selectAmount = selectAmount;
    }

    public Integer getSlot() {
        return slot;
    }

    public BigInteger getPrice() {
        return price;
    }

    public ItemStack getDisplay() {
        return display;
    }

    public ItemStack getShopItem() {
        return shopItem;
    }

    public List<String> getCommands() {
        return commands;
    }

    public Boolean needSelectAmount() {
        return selectAmount;
    }
}