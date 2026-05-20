package com.rtpplugin.rtp.gui;

import com.rtpplugin.rtp.RTPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class RTPGui {

    public static final String GUI_TITLE_RAW = "rtp_world_select";

    public static void open(Player player, RTPPlugin plugin) {
        String rawTitle = plugin.getConfig().getString("messages.gui_title", "&8&lChoose a World");
        String title = color(rawTitle);

        Inventory inv = Bukkit.createInventory(null, 27, title);

        // Overworld — slot 11
        if (isEnabled(plugin, "overworld")) {
            inv.setItem(11, buildItem(plugin, "overworld", Material.GRASS_BLOCK));
        }

        // Nether — slot 13
        if (isEnabled(plugin, "nether")) {
            inv.setItem(13, buildItem(plugin, "nether", Material.NETHERRACK));
        }

        // End — slot 15
        if (isEnabled(plugin, "end")) {
            inv.setItem(15, buildItem(plugin, "end", Material.END_STONE));
        }

        // Fill empty slots with glass panes
        ItemStack filler = buildFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        player.openInventory(inv);
    }

    private static ItemStack buildItem(RTPPlugin plugin, String key, Material material) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("worlds." + key);
        if (sec == null) return new ItemStack(material);

        String displayName = color("&6" + sec.getString("display_name", key));
        int worldSize = sec.getInt("world_size", 10000);
        int minDist = sec.getInt("min_distance", 500);
        int maxDist = sec.getInt("max_distance", 5000);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(
                color("&7This world is &e" + worldSize + " &7blocks wide."),
                color("&7Land range: &e" + minDist + "&7-&e" + maxDist + " &7blocks from center."),
                color(""),
                color("&aClick to teleport!")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildFiller() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        return pane;
    }

    private static boolean isEnabled(RTPPlugin plugin, String key) {
        return plugin.getConfig().getBoolean("worlds." + key + ".enabled", true);
    }

    public static String color(String s) {
        return s.replace("&", "\u00A7");
    }
}
