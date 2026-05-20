package com.rtpplugin.rtp.managers;

import com.rtpplugin.rtp.RTPPlugin;
import com.rtpplugin.rtp.utils.SafeLocationFinder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    private final RTPPlugin plugin;

    // Tracks players currently in a countdown
    private final Map<UUID, BukkitTask> pendingTasks = new HashMap<>();
    private final Map<UUID, String> pendingWorld = new HashMap<>();

    public TeleportManager(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isPendingTeleport(Player player) {
        return pendingTasks.containsKey(player.getUniqueId());
    }

    public void beginCountdown(Player player, String worldKey) {
        if (isPendingTeleport(player)) return;

        int countdownSeconds = plugin.getConfig().getInt("pre_teleport_countdown", 5);

        pendingWorld.put(player.getUniqueId(), worldKey);

        // Array to hold current countdown value (effectively final workaround)
        int[] remaining = {countdownSeconds};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cancelCountdown(player, false);
                return;
            }

            if (remaining[0] <= 0) {
                // Countdown finished — teleport
                finishTeleport(player, worldKey);
                return;
            }

            // Show action bar countdown
            String template = plugin.getConfig().getString("messages.teleporting",
                    "&aTeleporting in &e{countdown}&a... &7(Don't move!)");
            String msg = template.replace("{countdown}", String.valueOf(remaining[0]));
            player.sendActionBar(colorComponent(msg));

            remaining[0]--;

        }, 0L, 20L); // every second

        pendingTasks.put(player.getUniqueId(), task);
    }

    public void cancelCountdown(Player player, boolean notifyPlayer) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = pendingTasks.remove(uuid);
        pendingWorld.remove(uuid);

        if (task != null) {
            task.cancel();
        }

        if (notifyPlayer && player.isOnline()) {
            String msg = plugin.getConfig().getString("messages.cancelled",
                    "&cTeleportation cancelled! You moved.");
            player.sendActionBar(colorComponent(msg));
        }
    }

    public void cancelAll() {
        for (BukkitTask task : pendingTasks.values()) {
            task.cancel();
        }
        pendingTasks.clear();
        pendingWorld.clear();
    }

    private void finishTeleport(Player player, String worldKey) {
        // Remove from pending immediately so movement won't re-cancel
        UUID uuid = player.getUniqueId();
        BukkitTask task = pendingTasks.remove(uuid);
        pendingWorld.remove(uuid);
        if (task != null) task.cancel();

        // Run safe-location search async so we don't freeze the server
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String worldName = plugin.getConfig().getString("worlds." + worldKey + ".world_name", "world");
            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(colorComponent("&cWorld not found: " + worldName)));
                return;
            }

            int minDist = plugin.getConfig().getInt("worlds." + worldKey + ".min_distance", 500);
            int maxDist = plugin.getConfig().getInt("worlds." + worldKey + ".max_distance", 5000);
            int maxAttempts = plugin.getConfig().getInt("max_find_attempts", 50);

            Location safe = SafeLocationFinder.find(world, minDist, maxDist, maxAttempts);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (safe == null) {
                    String msg = plugin.getConfig().getString("messages.no_safe_spot",
                            "&cCould not find a safe location. Please try again.");
                    player.sendMessage(colorComponent(msg));
                    return;
                }

                player.teleport(safe);

                // Apply cooldown
                plugin.getCooldownManager().setCooldown(player);

                String displayName = plugin.getConfig().getString("worlds." + worldKey + ".display_name", worldKey);
                String msg = plugin.getConfig().getString("messages.teleported",
                        "&aTeleported to &e{world}&a!")
                        .replace("{world}", displayName);
                player.sendMessage(colorComponent(msg));
                player.sendActionBar(colorComponent(msg));
            });
        });
    }

    private Component colorComponent(String s) {
        // Simple & code → Adventure component conversion
        String colored = s.replace("&", "\u00A7");
        // Use legacy deserialization via BungeeCord compat layer (Paper supports this)
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacySection().deserialize(colored);
    }
}
