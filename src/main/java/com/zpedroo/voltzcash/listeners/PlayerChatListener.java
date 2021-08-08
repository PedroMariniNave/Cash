package com.zpedroo.voltzcash.listeners;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.zpedroo.voltzcash.VoltzCash;
import com.zpedroo.voltzcash.category.CategoryItem;
import com.zpedroo.voltzcash.utils.config.Messages;
import com.zpedroo.voltzcash.utils.menus.Menus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class PlayerChatListener implements Listener {

    private static HashMap<Player, PlayerChat> playerChat;

    static {
        playerChat = new HashMap<>(16);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(ChatMessageEvent event) {
        if (!getPlayerChat().containsKey(event.getSender())) return;

        event.setCancelled(true);

        PlayerChat playerChat = getPlayerChat().remove(event.getSender());
        Player player = playerChat.getPlayer();

        Integer amount = null;
        try {
            amount = Integer.parseInt(event.getMessage());
        } catch (Exception ex) {
            // ignore
        }

        if (amount == null || amount <= 0) {
            player.sendMessage(Messages.INVALID_AMOUNT);
            return;
        }

        CategoryItem categoryItem = playerChat.getCategoryItem();

        final Integer finalAmount = amount;
        VoltzCash.get().getServer().getScheduler().runTaskLater(VoltzCash.get(), () -> Menus.getInstance().openConfirmMenu(player, categoryItem, finalAmount), 0L);
    }

    public static HashMap<Player, PlayerChat> getPlayerChat() {
        return playerChat;
    }

    public static class PlayerChat {

        private Player player;
        private CategoryItem categoryItem;

        public PlayerChat(Player player, CategoryItem categoryItem) {
            this.player = player;
            this.categoryItem = categoryItem;
        }

        public Player getPlayer() {
            return player;
        }

        public CategoryItem getCategoryItem() {
            return categoryItem;
        }
    }
}