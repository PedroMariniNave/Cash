package com.zpedroo.voltzcash.utils.menus;

import com.zpedroo.voltzcash.category.Category;
import com.zpedroo.voltzcash.category.CategoryItem;
import com.zpedroo.voltzcash.listeners.PlayerChatListener;
import com.zpedroo.voltzcash.managers.CategoryManager;
import com.zpedroo.voltzcash.managers.DataManager;
import com.zpedroo.voltzcash.player.PlayerData;
import com.zpedroo.voltzcash.purchases.Purchase;
import com.zpedroo.voltzcash.transactions.Transaction;
import com.zpedroo.voltzcash.utils.FileUtils;
import com.zpedroo.voltzcash.utils.builder.InventoryBuilder;
import com.zpedroo.voltzcash.utils.builder.InventoryUtils;
import com.zpedroo.voltzcash.utils.builder.ItemBuilder;
import com.zpedroo.voltzcash.utils.config.Messages;
import com.zpedroo.voltzcash.utils.formatter.NumberFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Menus {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private InventoryUtils inventoryUtils;

    public Menus() {
        instance = this;
        this.inventoryUtils = new InventoryUtils();
    }

    public void openConfirmMenu(Player player, CategoryItem categoryItem, Integer amount) {
        FileUtils.Files file = FileUtils.Files.CONFIRM;

        int size = FileUtils.get().getInt(file, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            if (items == null) continue;

            BigInteger finalPrice = categoryItem.getPrice().multiply(BigInteger.valueOf(amount));

            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items, new String[]{
                    "{price}",
                    "{amount}"
            }, new String[]{
                    NumberFormatter.getInstance().format(finalPrice),
                    NumberFormatter.getInstance().formatDecimal(amount.doubleValue())
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String actionStr = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            if (!StringUtils.equals(actionStr, "NULL")) {
                switch (actionStr) {
                    case "CONFIRM" -> getInventoryUtils().addAction(inventory, item, () -> {
                        player.closeInventory();

                        PlayerData data = DataManager.getInstance().load(player);
                        if (data == null) return;

                        if (data.getCash().compareTo(finalPrice) < 0) {
                            player.sendMessage(Messages.INSUFFICIENT_CASH);
                            return;
                        }

                        data.removeCash(finalPrice);
                        if (categoryItem.getShopItem() != null) {
                            for (int i = 0; i < amount; ++i) {
                                player.getInventory().addItem(categoryItem.getShopItem());
                            }
                        }

                        for (String cmd : categoryItem.getCommands()) {
                            if (cmd == null) continue;

                            for (int i = 0; i < amount; ++i) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(cmd, new String[]{
                                        "{player}",
                                        "{amount}"
                                }, new String[]{
                                        player.getName(),
                                        amount.toString()
                                }));
                            }
                        }

                        for (String msg : Messages.SUCCESSFUL_PURCHASED) {
                            if (msg == null) continue;

                            player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                                    "{item}",
                                    "{amount}",
                                    "{price}"
                            }, new String[]{
                                    categoryItem.getDisplay().hasItemMeta() ? categoryItem.getDisplay().getItemMeta().hasDisplayName() ? categoryItem.getDisplay().getItemMeta().getDisplayName() : categoryItem.getDisplay().getType().toString() : categoryItem.getDisplay().getType().toString(),
                                    NumberFormatter.getInstance().formatDecimal(amount.doubleValue()),
                                    NumberFormatter.getInstance().format(finalPrice)
                            }));
                        }

                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 100f);

                        Integer id = FileUtils.get().getInt(FileUtils.Files.PURCHASES, "last-id") + 1;
                        Date date = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                        FileUtils.get().addLog("[" + format.format(date) + "]: BUY | " + player.getName() + " -> " + player.getName() + " (" + (categoryItem.getDisplay().hasItemMeta() ? categoryItem.getDisplay().getItemMeta().hasDisplayName() ? categoryItem.getDisplay().getItemMeta().getDisplayName() : categoryItem.getDisplay().getType().toString() : categoryItem.getDisplay().getType().toString()) + ") [ID: " + id + "]");
                        FileUtils.get().getFile(FileUtils.Files.PURCHASES).get().set("last-id", id);
                        FileUtils.get().getFile(FileUtils.Files.PURCHASES).save();

                        data.addPurchase(categoryItem.getDisplay().hasItemMeta() ? categoryItem.getDisplay().getItemMeta().hasDisplayName() ? categoryItem.getDisplay().getItemMeta().getDisplayName() : categoryItem.getDisplay().getType().toString() : categoryItem.getDisplay().getType().toString(), amount, finalPrice, id);
                    }, InventoryUtils.ActionClick.ALL);
                    case "CANCEL" -> getInventoryUtils().addAction(inventory, item, player::closeInventory, InventoryUtils.ActionClick.ALL);
                }
            }

            inventory.setItem(slot, item);
        }

        ItemStack shopItem = categoryItem.getDisplay().clone();
        ItemMeta meta = shopItem.getItemMeta();

        meta.setLore(null);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        shopItem.setItemMeta(meta);
        shopItem.setAmount(amount);

        int itemSlot = FileUtils.get().getInt(file, "Inventory.item-slot");
        inventory.setItem(itemSlot, shopItem);

        player.openInventory(inventory);
    }

    public void openShopMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.SHOP;

        int size = FileUtils.get().getInt(file, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            if (items == null) continue;

            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String actionStr = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            if (!StringUtils.equals(actionStr, "NULL")) {
                if (actionStr.contains("OPEN:")) {
                    String categoryName = actionStr.split(":")[1];
                    Category category = CategoryManager.getInstance().getCategoryDataCache().getCategory(categoryName);
                    if (category == null) continue;

                    getInventoryUtils().addAction(inventory, item, () -> {
                        openCategoryMenu(player, category);
                    }, InventoryUtils.ActionClick.ALL);
                }
            }

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    public void openInfoMenu(Player player, OfflinePlayer target) {
        int size = FileUtils.get().getInt(FileUtils.Files.INFO, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.INFO, "Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (String str : FileUtils.get().getSection(FileUtils.Files.INFO, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.INFO).get(), "Inventory.items." + str, new String[]{
                    "{player}",
                    "{cash}"
            }, new String[]{
                    target.getName(),
                    NumberFormatter.getInstance().format(DataManager.getInstance().load(target).getCash())
            }).build();
            int slot = FileUtils.get().getInt(FileUtils.Files.INFO, "Inventory.items." + str + ".slot");

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    public void openCategoryMenu(Player player, Category category) {
        int size = category.getSize();
        String title = category.getTitle();
        Inventory inventory = Bukkit.createInventory(null, size, title);

        List<ItemBuilder> builders = new ArrayList<>(64);
        for (CategoryItem item : category.getItems()) {
            if (item == null) continue;

            ItemStack display = item.getDisplay().clone();
            int slot = item.getSlot();
            InventoryUtils.Action action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, display, () -> {
                if (!item.needSelectAmount()) {
                    openConfirmMenu(player, item, 1);
                    return;
                }

                player.closeInventory();

                for (int i = 0; i < 25; ++i) {
                    player.sendMessage("");
                }

                for (String msg : Messages.CHOOSE_AMOUNT) {
                    if (msg == null) continue;

                    player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                            "{item}",
                            "{price}"
                    }, new String[]{
                            item.getDisplay().hasItemMeta() ? item.getDisplay().getItemMeta().hasDisplayName() ? item.getDisplay().getItemMeta().getDisplayName() : item.getDisplay().getType().toString() : item.getDisplay().getType().toString(),
                            NumberFormatter.getInstance().format(item.getPrice())
                    }));
                }

                PlayerChatListener.getPlayerChat().put(player, new PlayerChatListener.PlayerChat(player, item));
            });

            builders.add(ItemBuilder.build(display, slot, action));
        }

        InventoryBuilder.build(player, inventory, title, builders);
    }

    public void openMainMenu(Player player) {
        int size = FileUtils.get().getInt(FileUtils.Files.MAIN, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.MAIN, "Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);
        List<ItemBuilder> builders = new ArrayList<>(32);

        for (String str : FileUtils.get().getSection(FileUtils.Files.MAIN, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.MAIN).get(), "Inventory.items." + str, new String[]{
                    "{player}",
                    "{cash}"
            }, new String[]{
                    player.getName(),
                    NumberFormatter.getInstance().format(DataManager.getInstance().load(player).getCash())
            }).build();
            int slot = FileUtils.get().getInt(FileUtils.Files.MAIN, "Inventory.items." + str + ".slot");
            String actionStr = FileUtils.get().getString(FileUtils.Files.MAIN, "Inventory.items." + str + ".action");
            InventoryUtils.Action action = null;

            if (!StringUtils.equals(actionStr, "NULL")) {
                switch (actionStr) {
                    case "TRANSACTIONS" -> action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, item, () -> {
                        openTransactionsMenu(player);
                    });
                    case "TOP" -> action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, item, () -> {
                        openTopMenu(player);
                    });
                    case "SHOP" -> action = new InventoryUtils.Action(InventoryUtils.ActionClick.ALL, item, () -> {
                        openShopMenu(player);
                    });
                }
            }

            builders.add(ItemBuilder.build(item, slot, action));
        }

        InventoryBuilder.build(player, inventory, title, builders);
    }

    public void openPurchasesMenu(Player player) {
        int size = FileUtils.get().getInt(FileUtils.Files.PURCHASES, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.PURCHASES, "Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);
        List<ItemBuilder> builders = new ArrayList<>(256);
        List<Purchase> purchases = DataManager.getInstance().load(player).getPurchases();

        ItemStack item = null;

        if (purchases.size() > 0) {
            int i = -1;
            String[] slots = FileUtils.get().getString(FileUtils.Files.PURCHASES, "Inventory.slots").replace(" ", "").split(",");
            for (Purchase purchase : purchases) {
                if (++i >= slots.length) i = 0;

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.PURCHASES).get(), "Purchase-Item", new String[]{
                        "{item}",
                        "{price}",
                        "{amount}",
                        "{date}",
                        "{id}"
                }, new String[]{
                        purchase.getName(),
                        NumberFormatter.getInstance().format(purchase.getPrice()),
                        NumberFormatter.getInstance().formatDecimal(purchase.getAmount().doubleValue()),
                        dateFormat.format(purchase.getDate()),
                        purchase.getID().toString()
                }).build();
                int slot = Integer.parseInt(slots[i]);

                builders.add(ItemBuilder.build(item, slot, null));
            }
        } else {
            item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.PURCHASES).get(), "Empty").build();
            int slot = FileUtils.get().getInt(FileUtils.Files.PURCHASES, "Empty.slot");

            builders.add(ItemBuilder.build(item, slot, null));
        }

        ItemStack switchItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.PURCHASES).get(), "Switch").build();
        int switchSlot = FileUtils.get().getInt(FileUtils.Files.PURCHASES, "Switch.slot");

        inventory.setItem(switchSlot, switchItem);

        getInventoryUtils().addAction(inventory, switchItem, () -> {
            openTransactionsMenu(player);
        }, InventoryUtils.ActionClick.ALL);

        ItemStack nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.PURCHASES).get(), "Next-Page").build();
        ItemStack previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.PURCHASES).get(), "Previous-Page").build();

        int nextPageSlot = FileUtils.get().getInt(FileUtils.Files.PURCHASES, "Next-Page.slot");
        int previousPageSlot = FileUtils.get().getInt(FileUtils.Files.PURCHASES, "Previous-Page.slot");

        InventoryBuilder.build(player, inventory, title, builders, nextPageSlot, previousPageSlot, nextPageItem, previousPageItem);
    }

    public void openTransactionsMenu(Player player) {
        int size = FileUtils.get().getInt(FileUtils.Files.TRANSACTIONS, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.TRANSACTIONS, "Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);
        List<ItemBuilder> builders = new ArrayList<>(256);
        List<Transaction> transactions = DataManager.getInstance().load(player).getTransactions();

        ItemStack item = null;

        if (transactions.size() > 0) {
            int i = -1;
            String[] slots = FileUtils.get().getString(FileUtils.Files.TRANSACTIONS, "Inventory.slots").replace(" ", "").split(",");
            for (Transaction transaction : transactions) {
                if (++i >= slots.length) i = 0;

                String type = transaction.getType().toString().toLowerCase();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).get(), "Inventory.items." + type, new String[]{
                        "{actor}",
                        "{target}",
                        "{amount}",
                        "{date}",
                        "{id}"
                }, new String[]{
                        transaction.getActor().getName(),
                        transaction.getTarget().getName(),
                        NumberFormatter.getInstance().format(transaction.getAmount()),
                        dateFormat.format(transaction.getDate()),
                        transaction.getID().toString()
                }).build();
                int slot = Integer.parseInt(slots[i]);

                builders.add(ItemBuilder.build(item, slot, null));
            }
        } else {
            item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).get(), "Empty").build();
            int slot = FileUtils.get().getInt(FileUtils.Files.TRANSACTIONS, "Empty.slot");

            builders.add(ItemBuilder.build(item, slot, null));
        }

        ItemStack switchItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).get(), "Switch").build();
        int switchSlot = FileUtils.get().getInt(FileUtils.Files.TRANSACTIONS, "Switch.slot");

        inventory.setItem(switchSlot, switchItem);

        getInventoryUtils().addAction(inventory, switchItem, () -> {
            openPurchasesMenu(player);
        }, InventoryUtils.ActionClick.ALL);

        ItemStack nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).get(), "Next-Page").build();
        ItemStack previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TRANSACTIONS).get(), "Previous-Page").build();

        int nextPageSlot = FileUtils.get().getInt(FileUtils.Files.TRANSACTIONS, "Next-Page.slot");
        int previousPageSlot = FileUtils.get().getInt(FileUtils.Files.TRANSACTIONS, "Previous-Page.slot");

        InventoryBuilder.build(player, inventory, title, builders, nextPageSlot, previousPageSlot, nextPageItem, previousPageItem);
    }

    public void openTopMenu(Player player) {
        int size = FileUtils.get().getInt(FileUtils.Files.TOP, "Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.TOP, "Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);

        int pos = 0;
        String[] topSlots = FileUtils.get().getString(FileUtils.Files.TOP, "Inventory.slots").replace(" ", "").split(",");

        int slot = -1;
        ItemStack item = null;

        for (PlayerData data : DataManager.getInstance().getDataCache().getTop()) {
            slot = Integer.parseInt(topSlots[pos]);
            item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.TOP).get(), "Item", new String[]{
                    "{player}",
                    "{pos}",
                    "{cash}"
            }, new String[]{
                    Bukkit.getOfflinePlayer(data.getUUID()).getName(),
                    String.valueOf(++pos),
                    NumberFormatter.getInstance().format(data.getCash())
            }).build();

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    private InventoryUtils getInventoryUtils() {
        return inventoryUtils;
    }
}
