package org.pronze.bwaddon.listener;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.pronze.bwaddon.BwAddon;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.bukkit.inventory.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.screamingsandals.bedwars.api.TeamColor;
import org.screamingsandals.bedwars.api.game.*;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.utils.ColorChanger;
import org.bukkit.event.inventory.InventoryType.SlotType;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class PlayerListener implements Listener {
    org.pronze.bwaddon.BwAddon plugin;
    private BedwarsAPI api;
    static public HashMap<Player, List<ItemStack>> PlayerItems = new HashMap<>();
    static public ArrayList<Material> allowed = new ArrayList<>();

    public PlayerListener(org.pronze.bwaddon.BwAddon plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, BwAddon.getInstance());


        for (String material : BwAddon.getConfigurator().getStringList("allowed-item-drops")) {
            Material mat;
            try {
                mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
            } catch (Exception ignored) {
                continue;
            }
            allowed.add(mat);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent e) {

        api = BedwarsAPI.getInstance();


        Player player = e.getPlayer();


        if (!api.isPlayerPlayingAnyGame(player)) return;

        Game game = api.getGameOfPlayer(player);

        Team team = game.getTeamOfPlayer(player);


        new BukkitRunnable() {

            @Override
            public void run() {
                if (player.getGameMode() == GameMode.SURVIVAL && api.isPlayerPlayingAnyGame(player)) {
                    handleRespawn(player);
                    this.cancel();
                } else if (!api.isPlayerPlayingAnyGame(player))
                    this.cancel();
            }

        }.runTaskTimer(this.plugin, 20L, 20L);


    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {

        api = BedwarsAPI.getInstance();

        Player player = e.getEntity();


        if (!api.isPlayerPlayingAnyGame(player)) return;

        Game game = api.getGameOfPlayer(player);

        List<ItemStack> items = new ArrayList<>();
        ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
        ItemStack pickaxe = new ItemStack(Material.WOODEN_AXE);
        ItemStack axe = new ItemStack(Material.WOODEN_PICKAXE);

        boolean hasPickaxe = false, hasAxe = false;
        for (ItemStack newItem : player.getInventory().getContents()) {
            if (newItem != null && newItem.getType().name().endsWith("SWORD")) {
                if (newItem.getEnchantments().size() > 0)
                    sword.addEnchantments(newItem.getEnchantments());
            }
                else if (newItem != null && newItem.getType().name().endsWith("AXE")) {
                    if(newItem.getType().name().endsWith("PICKAXE")) {
                        pickaxe.addEnchantments(newItem.getEnchantments());
                        hasPickaxe = true;
                    }
                    else {
                        axe.addEnchantments(newItem.getEnchantments());
                        hasAxe = true;

                }
            }
        }
             items.add(sword);
            if(hasPickaxe)
                items.add(pickaxe);
            if(hasAxe)
                items.add(axe);

            PlayerItems.put(player, items);

            if (e.getEntity().getKiller() != null && plugin.getConfigurator().getBoolean("give-killer-resources", true)) {
                Player killer = e.getEntity().getKiller();
                for (ItemStack dropItem : player.getInventory().getContents()) {
                    if (dropItem != null && allowed.contains(dropItem.getType()) && !dropItem.getType().toString().endsWith("WOOL")) {
                        killer.sendMessage("You Recieved " + dropItem.getType().name() + " for killing " + player.getName());
                        killer.getInventory().addItem(dropItem);
                    }
                }
            }

            e.getDrops().clear();
        }

        public void handleRespawn (Player player)
        {
            List<ItemStack> items = PlayerItems.get(player);

            items.forEach(item -> {
                        if (item != null) {
                            if (item.getType().toString().contains("LEGGINGS")) {
                                player.getInventory().setLeggings(null);
                                player.getInventory().setLeggings(item);

                            } else if (item.getType().toString().contains("BOOTS")) {
                                player.getInventory().setBoots(null);
                                player.getInventory().setBoots(item);
                            }
                            else{
                                player.getInventory().addItem(item);
                            }
                        }
                    }
                );

            PlayerItems.remove(player);
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onClick (InventoryClickEvent event)
        {

            api = BedwarsAPI.getInstance();

            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();

            if (!api.isPlayerPlayingAnyGame(player)) return;

            if (event.getSlotType() == SlotType.ARMOR)
                event.setCancelled(true);
        }

        private void giveItemToPlayer (List < ItemStack > itemStackList, Player player, TeamColor teamColor){
            for (ItemStack itemStack : itemStackList) {

                api = BedwarsAPI.getInstance();
                if (!api.isPlayerPlayingAnyGame(player)) return;

                ColorChanger colorChanger = api.getColorChanger();

                final String materialName = itemStack.getType().toString();
                final PlayerInventory playerInventory = player.getInventory();

                if (materialName.contains("HELMET")) {
                    playerInventory.setHelmet(colorChanger.applyColor(teamColor, itemStack));
                } else if (materialName.contains("CHESTPLATE")) {
                    playerInventory.setChestplate(colorChanger.applyColor(teamColor, itemStack));
                } else if (materialName.contains("LEGGINGS")) {
                    playerInventory.setLeggings(colorChanger.applyColor(teamColor, itemStack));
                } else if (materialName.contains("BOOTS")) {
                    playerInventory.setBoots(colorChanger.applyColor(teamColor, itemStack));
                } else {
                    playerInventory.addItem(colorChanger.applyColor(teamColor, itemStack));
                }
            }
        }

        @EventHandler
        public void onItemDrop (PlayerDropItemEvent evt )
        {
            api = BedwarsAPI.getInstance();

            if (!api.isPlayerPlayingAnyGame(evt.getPlayer())) return;


            if (!allowed.contains(evt.getItemDrop().getItemStack().getType())) {
                evt.setCancelled(true);
                evt.getPlayer().getInventory().remove(evt.getItemDrop().getItemStack());
            }
        }


    }
