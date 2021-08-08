package com.zpedroo.voltzcash.commands;

import com.zpedroo.voltzcash.utils.menus.Menus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        Menus.getInstance().openShopMenu(player);
        return false;
    }
}