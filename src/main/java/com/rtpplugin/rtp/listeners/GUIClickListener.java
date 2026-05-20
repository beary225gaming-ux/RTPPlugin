package com.rtpplugin.rtp.listeners;

import com.rtpplugin.rtp.RTPPlugin;
import com.rtpplugin.rtp.gui.RTPGui;
import com.rtpplugin.rtp.managers.TeleportManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIClickListener implements Listener {

    private final RTPPlugin plugin;

    public GUIClickListener(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String guiTitle = RTPGui.color(plugin.getConfig().getString("messages.gui_title", "&8&lChoose a World"));
        if (!event.getView().getTitle().equals(guiTitle)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Material mat = clicked.getType();
        String worldKey;

        if (mat == Material.GRASS_BLOCK) {
            worldKey = "overworld";
        } else if (mat == Material.NETHERRACK) {
            worldKey = "nether";
        } else if (mat == Material.END_STONE) {
            worldKey = "end";
        } else {
            return;
        }

        player.closeInventory();

        TeleportManager tm = plugin.getTeleportManager();
        tm.beginCountdown(player, worldKey);
    }
}
