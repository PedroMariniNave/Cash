package com.zpedroo.voltzcash;

import com.zpedroo.voltzcash.commands.CashCmd;
import com.zpedroo.voltzcash.commands.ShopCmd;
import com.zpedroo.voltzcash.listeners.PlayerChatListener;
import com.zpedroo.voltzcash.listeners.PlayerGeneralListeners;
import com.zpedroo.voltzcash.managers.CategoryManager;
import com.zpedroo.voltzcash.managers.DataManager;
import com.zpedroo.voltzcash.mysql.DBConnection;
import com.zpedroo.voltzcash.tasks.TopTask;
import com.zpedroo.voltzcash.utils.FileUtils;
import com.zpedroo.voltzcash.utils.config.Settings;
import com.zpedroo.voltzcash.utils.formatter.NumberFormatter;
import com.zpedroo.voltzcash.utils.menus.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

public class VoltzCash extends JavaPlugin {

    private static VoltzCash instance;
    public static VoltzCash get() { return instance; }

    public void onEnable() {
        instance = this;
        new FileUtils(this);

        if (!isMySQLEnabled(getConfig())) {
            getLogger().log(Level.SEVERE, "MySQL are disabled! You need to enable it.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new DBConnection(getConfig());
        new TopTask(this);
        new DataManager();
        new NumberFormatter(getConfig());
        new CategoryManager();
        new Menus();

        registerCommands();
        registerListeners();
    }

    public void onDisable() {
        try {
            DataManager.getInstance().saveAll();
            DBConnection.getInstance().closeConnection();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "An error ocurred while trying to save data!");
        }
    }

    private void registerCommands() {
        registerCommand(Settings.CASH_CMD, Settings.CASH_ALIASES, new CashCmd());
        registerCommand(Settings.SHOP_CMD, Settings.SHOP_ALIASES, new ShopCmd());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerGeneralListeners(), this);
    }

    private void registerCommand(String command, List<String> aliases, CommandExecutor executor) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            PluginCommand pluginCmd = constructor.newInstance(command, this);
            pluginCmd.setAliases(aliases);
            pluginCmd.setExecutor(executor);

            Field field = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getPluginManager());
            commandMap.register(getName().toLowerCase(), pluginCmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Boolean isMySQLEnabled(FileConfiguration file) {
        if (!file.contains("MySQL.enabled")) return false;

        return file.getBoolean("MySQL.enabled");
    }
}