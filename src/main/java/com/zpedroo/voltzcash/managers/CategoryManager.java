package com.zpedroo.voltzcash.managers;

import com.zpedroo.voltzcash.VoltzCash;
import com.zpedroo.voltzcash.category.Category;
import com.zpedroo.voltzcash.category.CategoryItem;
import com.zpedroo.voltzcash.category.cache.CategoryDataCache;
import com.zpedroo.voltzcash.utils.builder.ItemBuilder;
import com.zpedroo.voltzcash.utils.formatter.NumberFormatter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CategoryManager {

    private static CategoryManager instance;
    public static CategoryManager getInstance() { return instance; }

    private CategoryDataCache categoryDataCache;

    public CategoryManager() {
        instance = this;
        this.categoryDataCache = new CategoryDataCache();
        this.loadCategories();
    }

    private void loadCategories() {
        File folder = new File(VoltzCash.get().getDataFolder(), "/categories");
        File[] files = folder.listFiles((d, name) -> name.endsWith(".yml"));

        if (files == null) return;
        for (File fl : files) {
            if (fl == null) continue;

            YamlConfiguration file = YamlConfiguration.loadConfiguration(fl);
            String name = fl.getName().replace(".yml", "");
            String title = ChatColor.translateAlternateColorCodes('&', file.getString("Inventory.title", "NULL"));
            Integer size = file.getInt("Inventory.size");
            List<CategoryItem> items = new ArrayList<>(64);

            for (String str : file.getConfigurationSection("Inventory.items").getKeys(false)) {
                if (str == null) continue;

                Integer slot = file.getInt("Inventory.items." + str + ".slot");
                BigInteger price = new BigInteger(file.getString("Inventory.items." + str + ".price", "0"));
                ItemStack display = ItemBuilder.build(file, "Inventory.items." + str + ".display", new String[]{
                        "{price}"
                }, new String[]{
                        NumberFormatter.getInstance().format(price),
                }).build();
                ItemStack shopItem = file.contains("Inventory.items." + str + ".shop-item") ? ItemBuilder.build(file, "Inventory.items." + str + ".shop-item").build() : null;
                List<String> commands = file.getStringList("Inventory.items." + str + ".commands");
                Boolean selectAmount = file.getBoolean("Inventory.items." + str + ".select-amount", false);

                items.add(new CategoryItem(slot, price, display, shopItem, commands, selectAmount));
            }

            Category category = new Category(title, size, items);
            cache(name, category);
        }
    }

    private void cache(String name, Category category) {
        getCategoryDataCache().getCategories().put(name, category);
    }

    public CategoryDataCache getCategoryDataCache() {
        return categoryDataCache;
    }
}