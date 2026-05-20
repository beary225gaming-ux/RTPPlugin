package com.rtpplugin.rtp;

import com.rtpplugin.rtp.commands.RTPCommand;
import com.rtpplugin.rtp.listeners.PlayerMoveListener;
import com.rtpplugin.rtp.listeners.GUIClickListener;
import com.rtpplugin.rtp.managers.CooldownManager;
import com.rtpplugin.rtp.managers.TeleportManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RTPPlugin extends JavaPlugin {

    private CooldownManager cooldownManager;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        cooldownManager = new CooldownManager(this);
        teleportManager = new TeleportManager(this);

        getCommand("rtp").setExecutor(new RTPCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIClickListener(this), this);

        getLogger().info("RTPPlugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (teleportManager != null) {
            teleportManager.cancelAll();
        }
        getLogger().info("RTPPlugin disabled.");
    }

    public CooldownManager getCooldownManager() { return cooldownManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
}
