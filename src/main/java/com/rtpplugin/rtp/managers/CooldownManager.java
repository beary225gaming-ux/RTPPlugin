package com.rtpplugin.rtp.managers;

import com.rtpplugin.rtp.RTPPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final RTPPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public CooldownManager(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) return false;
        return getRemainingCooldown(player) > 0;
    }

    public long getRemainingCooldown(Player player) {
        long endTime = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remaining = (endTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    public void setCooldown(Player player) {
        int cooldownSeconds = plugin.getConfig().getInt("cooldown", 300);
        if (cooldownSeconds <= 0) return;
        long endTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        cooldowns.put(player.getUniqueId(), endTime);
    }

    public void clearCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    public boolean hasBypassPermission(Player player) {
        List<String> perms = plugin.getConfig().getStringList("bypass_permissions");
        for (String perm : perms) {
            if (player.hasPermission(perm)) return true;
        }
        return false;
    }
}
