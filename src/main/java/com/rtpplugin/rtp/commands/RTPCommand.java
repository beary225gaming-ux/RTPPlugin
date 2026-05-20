package com.rtpplugin.rtp.commands;

import com.rtpplugin.rtp.RTPPlugin;
import com.rtpplugin.rtp.gui.RTPGui;
import com.rtpplugin.rtp.managers.CooldownManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPCommand implements CommandExecutor {

    private final RTPPlugin plugin;

    public RTPCommand(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /rtp.");
            return true;
        }

        if (!player.hasPermission("rtp.use")) {
            player.sendMessage(color("&cYou don't have permission to use /rtp."));
            return true;
        }

        CooldownManager cm = plugin.getCooldownManager();

        // Check cooldown (skip if player has a bypass permission)
        if (!cm.hasBypassPermission(player) && cm.isOnCooldown(player)) {
            long remaining = cm.getRemainingCooldown(player);
            String msg = plugin.getConfig().getString("messages.cooldown", "&cWait {time}s.")
                    .replace("{time}", String.valueOf(remaining));
            player.sendMessage(color(msg));
            return true;
        }

        // Open the world selection GUI
        RTPGui.open(player, plugin);
        return true;
    }

    public static String color(String s) {
        return s.replace("&", "\u00A7");
    }
}
