package org.pronze.bwaddon.listener;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
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
import org.screamingsandals.bedwars.api.upgrades.Upgrade;
import org.screamingsandals.bedwars.api.utils.ColorChanger;
import org.bukkit.event.inventory.InventoryType.SlotType;

import java.util.*;

public class PlayerListener implements Listener {
    org.pronze.bwaddon.BwAddon plugin;
    private BedwarsAPI api;
    static public HashMap<Player, List<ItemStack>> PlayerItems = new HashMap<>();
    static public HashMap<String, Integer> UpgradeKeys = new HashMap<>();
    static public ArrayList<Material> allowed = new ArrayList<>();

    public PlayerListener(org.pronze.bwaddon.BwAddon plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, BwAddon.getInstance());

        //Initalizing UpgradeKey Variables for easier handling of downgrading equipments.
        UpgradeKeys.put("WOODEN", 1);
        UpgradeKeys.put("STONE", 2);
        UpgradeKeys.put("GOLDEN", 3);
        UpgradeKeys.put("IRON", 4);
        UpgradeKeys.put("DIAMOND", 5);

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

    public static <K, V> K getKey(HashMap<K, V> map, V value) {
        for (K key : map.keySet()) {
            if (value.equals(map.get(key))) {
                return key;
            }
        }
        return null;
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
                    giveItemToPlayer(PlayerItems.get(player), player, team.getColor());
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

        for (ItemStack newItem : player.getInventory().getContents()) {
            if(newItem != null) {
                if (newItem.getType().name().endsWith("SWORD")) {
                    if (newItem.getEnchantments().size() > 0)
                        sword.addEnchantments(newItem.getEnchantments());
                } else if (newItem.getType().name().endsWith("AXE")) {
                    newItem = checkifUpgraded(newItem);
                    items.add(newItem);
                } else if (newItem.getType().name().contains("LEGGINGS") ||
                        newItem.getType().name().contains("BOOTS") ||
                        newItem.getType().name().contains("CHESTPLATE") ||
                        newItem.getType().name().contains("HELMET"))
                    items.add(newItem);
            }
        }
             items.add(sword);
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

        public ItemStack checkifUpgraded(ItemStack newItem)
        {
            if(UpgradeKeys.get(newItem.getType().name().substring(0, newItem.getType().name().indexOf("_") )) > UpgradeKeys.get("WOODEN"))
            {
                Map<Enchantment, Integer> enchant = newItem.getEnchantments();
                Material mat = null;
                mat =  mat.valueOf(getKey(UpgradeKeys, UpgradeKeys.get(newItem.getType().name().substring(0, newItem.getType().name().indexOf("_") )) - 1) + newItem.getType().name().substring(newItem.getType().name().lastIndexOf("_")));
                ItemStack temp  = new ItemStack(mat);
                temp.addEnchantments(enchant);
                return temp;
            }
            return newItem;
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onClick (InventoryClickEvent event)
        {
            if(event.getCurrentItem() == null)
                return;
            api = BedwarsAPI.getInstance();

            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();

            if (!api.isPlayerPlayingAnyGame(player)) return;

            if (event.getSlotType() == SlotType.ARMOR)
                event.setCancelled(true);


            Inventory topSlot = event.getView().getTopInventory();
           Inventory bottomSlot = event.getView().getBottomInventory();
           if( event.getClickedInventory().equals(bottomSlot) && BwAddon.getConfigurator().getBoolean("block-players-putting-certain-items-onto-chest" , true) && (topSlot.getType() == InventoryType.CHEST || topSlot.getType() == InventoryType.ENDER_CHEST ) && bottomSlot.getType() == InventoryType.PLAYER)
           {
               if(event.getCurrentItem().getType().name().endsWith("AXE") || event.getCurrentItem().getType().name().endsWith("SWORD")) {
                   event.setResult(Event.Result.DENY);
                   player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "You cannot put this item onto this chest.");
               }
           }
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
                } else if (materialName.contains("PICKAXE")){
                    playerInventory.setItem(7,  itemStack);
                } else if (materialName.contains("AXE")){
                    playerInventory.setItem(8,  itemStack);
                } else if (materialName.contains("SWORD")){
                    playerInventory.setItem(0,  itemStack);
                }

                else {
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
