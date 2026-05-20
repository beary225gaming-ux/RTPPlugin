package com.rtpplugin.rtp.listeners;

import com.rtpplugin.rtp.RTPPlugin;
import com.rtpplugin.rtp.managers.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerMoveListener implements Listener {

    private final RTPPlugin plugin;

    public PlayerMoveListener(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        TeleportManager tm = plugin.getTeleportManager();

        if (!tm.isPendingTeleport(player)) return;

        // Only cancel if they actually moved position (not just looked around)
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

            tm.cancelCountdown(player, true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TeleportManager tm = plugin.getTeleportManager();
        if (tm.isPendingTeleport(player)) {
            tm.cancelCountdown(player, false);
        }
    }
}
