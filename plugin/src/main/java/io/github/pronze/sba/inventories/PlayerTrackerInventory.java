package io.github.pronze.sba.inventories;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;

import io.github.pronze.sba.SBA;

public class PlayerTrackerInventory implements InventoryHolder {

    private Game game;
    private Consumer<Player> targetSelector;
    private List<Player> displayed;
    private String title;

    public PlayerTrackerInventory(Game game, String title, Consumer<Player> targetSelector) {
        this.game = game;
        this.title = title;
        this.targetSelector = targetSelector;

    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, InventoryType.PLAYER,title);//Bukkit.createInventory(this, 9 * 6, "--");

        Bukkit.getScheduler().runTaskAsynchronously(SBA.getPluginInstance(), () -> addContent(inventory));

        return inventory;
    }

    /**
     * Called when there is a click in this GUI.
     * Cancelled automatically.
     *
     * @param event The InventoryClickEvent provided by Bukkit
     */
    public void onInventoryClick(InventoryClickEvent event) {
        Player target = displayed.get(event.getSlot());
        event.getWhoClicked().closeInventory();
        event.setCancelled(true);
        targetSelector.accept(target);
    }

    /**
     * Called when updating the Inventories contents
     */
    public void addContent(Inventory inventory) {
        int i = 0;
        Material mat = null;
        mat = Material.matchMaterial("PLAYER_HEAD");
        if (mat == null) {
            mat = Material.matchMaterial("SKULL_ITEM");
        }
        if (game != null)
            displayed = new ArrayList<>(game.getConnectedPlayers());
        else
            displayed = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (Player p : displayed) {
            if(i>=9*4) break;
            ItemStack is = null;
            if (mat.toString() == "SKULL_ITEM")
                is = new ItemStack(mat, 1, (short) 3);
            else
                is = new ItemStack(mat);
            SkullMeta meta = (SkullMeta) is.getItemMeta();
            meta.setOwner(p.getName());
            meta.setDisplayName(p.getDisplayName());
            is.setItemMeta(meta);
            inventory.setItem(i++, is);
        }
    }

    public PlayerTrackerInventory openForPlayer(@NotNull Player player) {
        player.openInventory(this.getInventory());
        return this;
    }
}
