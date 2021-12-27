package io.github.pronze.sba.util;

import io.github.pronze.sba.SBWAddonAPI;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.utils.reflect.Reflect;

import java.util.logging.Level;

@UtilityClass
public class SBAUtil {

    public void disablePlugin(@NotNull JavaPlugin plugin) {
        // thank you Misat for this :)
        try {
            final var message = String.format("Disabling %s", plugin.getDescription().getFullName());
            plugin.getLogger().info(message);
            Bukkit.getPluginManager().callEvent(new PluginDisableEvent(plugin));
            Reflect.setField(plugin, "isEnabled", false);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            Bukkit.getScheduler().cancelTasks(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while cancelling tasks for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            Bukkit.getServicesManager().unregisterAll(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering services for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            HandlerList.unregisterAll(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering events for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin);
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering plugin channels for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            for (World world : Bukkit.getWorlds()) {
                world.removePluginChunkTickets(plugin);
            }
        } catch (Throwable ex) {
            // older versions don't even have chunk tickets
            //Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while removing chunk tickets for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

    public void reloadPlugin(@NonNull JavaPlugin plugin) {
        disablePlugin(plugin);
        Bukkit.getServer().getPluginManager().enablePlugin(plugin);
        if (plugin == SBWAddonAPI.getInstance().as(JavaPlugin.class)) {
            // TODO: reload language and configuration.
        }
        Bukkit.getLogger().info("Plugin reloaded! Keep in mind that restarting the server is safer!");
    }
}
