package org.pronze.hypixelify.listener;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.arena.Arena;
import org.pronze.hypixelify.utils.ScoreboardUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.*;
import org.bukkit.inventory.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.*;
import org.screamingsandals.bedwars.api.utils.ColorChanger;
import org.bukkit.event.inventory.InventoryType.SlotType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class PlayerListener implements Listener {
    Hypixelify plugin;
    private BedwarsAPI api;
    static public HashMap<Player, List<ItemStack>> PlayerItems = new HashMap<>();
    static public HashMap<String, Integer> UpgradeKeys = new HashMap<>();
    static public ArrayList<Material> allowed = new ArrayList<>();
    static public ArrayList<Material> generatorDropItems = new ArrayList<>();

    public PlayerListener(Hypixelify plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, Hypixelify.getInstance());

        //Initalizing UpgradeKey Variables for easier handling of downgrading equipments.
        UpgradeKeys.put("WOODEN", 1);
        UpgradeKeys.put("STONE", 2);
        UpgradeKeys.put("GOLDEN", 3);
        UpgradeKeys.put("IRON", 4);
        UpgradeKeys.put("DIAMOND", 5);

        for (String material : Hypixelify.getConfigurator().getStringList("allowed-item-drops")) {
            Material mat;
            try {
                mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
            } catch (Exception ignored) {
                continue;
            }
            allowed.add(mat);
        }
        for (String material : Hypixelify.getConfigurator().getStringList("running-generator-drops")) {
            Material mat;
            try {
                mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
            } catch (Exception ignored) {
                continue;
            }
            generatorDropItems.add(mat);
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
                if (player.getGameMode().equals(GameMode.SURVIVAL) && api.isPlayerPlayingAnyGame(player)) {
                    giveItemToPlayer(PlayerItems.get(player), player, team.getColor());
                    this.cancel();
                } else if (!api.isPlayerPlayingAnyGame(player))
                    this.cancel();
            }

        }.runTaskTimer(this.plugin, 20L, 20L);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent e) {

        api = BedwarsAPI.getInstance();

        Player player = e.getEntity();


        if (!api.isPlayerPlayingAnyGame(player)) return;


        Game game = api.getGameOfPlayer(player);

        if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName()))
            (Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName())).onDeath(player);

        List<ItemStack> items = new ArrayList<>();
        ItemStack sword = new ItemStack(Material.WOODEN_SWORD);

        for (ItemStack newItem : player.getInventory().getContents()) {
            if (newItem != null) {
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

        if (e.getEntity().getKiller() != null && BedwarsAPI.getInstance().isPlayerPlayingAnyGame(e.getEntity().getKiller()) && e.getEntity().getKiller().getGameMode().equals(GameMode.SURVIVAL)
                && Hypixelify.getConfigurator().config.getBoolean("give-killer-resources", true)) {
            Player killer = e.getEntity().getKiller();
            for (ItemStack dropItem : player.getInventory().getContents()) {
                if (dropItem != null && generatorDropItems.contains(dropItem.getType())) {
                    killer.sendMessage("+" + dropItem.getAmount() + " " + dropItem.getType().name());
                    killer.getInventory().addItem(dropItem);
                }
            }
        }

        if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName()))
            Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).onDeath(player);
    }

    public ItemStack checkifUpgraded(ItemStack newItem) {
        if (UpgradeKeys.get(newItem.getType().name().substring(0, newItem.getType().name().indexOf("_"))) > UpgradeKeys.get("WOODEN")) {
            Map<Enchantment, Integer> enchant = newItem.getEnchantments();
            Material mat;
            mat = Material.valueOf(getKey(UpgradeKeys, UpgradeKeys.get(newItem.getType().name().substring(0, newItem.getType().name().indexOf("_"))) - 1) + newItem.getType().name().substring(newItem.getType().name().lastIndexOf("_")));
            ItemStack temp = new ItemStack(mat);
            temp.addEnchantments(enchant);
            return temp;
        }
        return newItem;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;
        api = BedwarsAPI.getInstance();

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (!api.isPlayerPlayingAnyGame(player)) return;

        if (Hypixelify.getConfigurator().config.getBoolean("disable-armor-inventory-movement", true) && event.getSlotType() == SlotType.ARMOR)
            event.setCancelled(true);

        Inventory topSlot = event.getView().getTopInventory();
        Inventory bottomSlot = event.getView().getBottomInventory();
        if (event.getClickedInventory().equals(bottomSlot) && Hypixelify.getConfigurator().getBoolean("block-players-putting-certain-items-onto-chest", true) && (topSlot.getType() == InventoryType.CHEST || topSlot.getType() == InventoryType.ENDER_CHEST) && bottomSlot.getType() == InventoryType.PLAYER) {
            if (event.getCurrentItem().getType().name().endsWith("AXE") || event.getCurrentItem().getType().name().endsWith("SWORD")) {
                event.setResult(Event.Result.DENY);
                player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "You cannot put this item onto this chest.");
            }
        }
    }

    private void giveItemToPlayer(List<ItemStack> itemStackList, Player player, TeamColor teamColor) {
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
            } else if (materialName.contains("PICKAXE")) {
                playerInventory.setItem(7, itemStack);
            } else if (materialName.contains("AXE")) {
                playerInventory.setItem(8, itemStack);
            } else if (materialName.contains("SWORD")) {
                playerInventory.setItem(0, itemStack);
            } else {
                playerInventory.addItem(colorChanger.applyColor(teamColor, itemStack));
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent evt) {
        api = BedwarsAPI.getInstance();

        if (!api.isPlayerPlayingAnyGame(evt.getPlayer())) return;

        if (!allowed.contains(evt.getItemDrop().getItemStack().getType()) && !evt.getItemDrop().getItemStack().getType().name().endsWith("WOOL")) {
            evt.setCancelled(true);
            evt.getPlayer().getInventory().remove(evt.getItemDrop().getItemStack());
        }
    }

    @EventHandler(priority =  EventPriority.LOWEST)
    public void onPlayerLeave(BedwarsPlayerLeaveEvent e){
        Player player = e.getPlayer();
        ScoreboardUtil.removePlayer(player);
    }

    @EventHandler
    public void itemDamage(PlayerItemDamageEvent e){
        Player player = e.getPlayer();
        if(!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)) return;
        if(!BedwarsAPI.getInstance().getGameOfPlayer(player).isPlayerInAnyTeam(player)) return;
        if(Main.getPlayerGameProfile(player).isSpectator) return;

        if(e.getItem().getType().toString().contains("BOOTS")
        || e.getItem().getType().toString().contains("HELMET")
        || e.getItem().getType().toString().contains("LEGGINGS")
        || e.getItem().getType().toString().contains("CHESTPLATE")
        || e.getItem().getType().toString().contains("SWORD"))
        {
            e.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStarted(BedwarsGameStartedEvent e) {
        final Game game = e.getGame();
        Map<Player, Scoreboard> scoreboards = ScoreboardUtil.getScoreboards();
        for (Player player : game.getConnectedPlayers()) {
            if (scoreboards.containsKey(player))
                ScoreboardUtil.removePlayer(player);
        }
        Arena arena = new Arena(game);
        Hypixelify.getInstance().getArenaManager().addArena(game.getName(), arena);
        (new BukkitRunnable() {
            public void run() {
                if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName()))
                    Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).getScoreBoard().updateScoreboard();
            }
        }).runTaskLater(Hypixelify.getInstance(), 2L);

        Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).onGameStarted(e);
    }

    @EventHandler
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final Game game = e.getGame();
        if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName())) {
            Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).onTargetBlockDestroyed(e);
            (new BukkitRunnable() {
                public void run() {
                    if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName()))
                        (Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName())).getScoreBoard().updateScoreboard();
                }
            }).runTaskLater(Hypixelify.getInstance(), 1L);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if(!p.isOp())
            return;

        if(!Hypixelify.getConfigurator().config.getString("version").contains(Hypixelify.getVersion())){
            new BukkitRunnable(){
                @Override
                public void run() {
                    p.sendMessage(ChatColor.GOLD + "[SBAHypixelify]: Plugin has detected a version change, do you want to upgrade internal files?");
                    p.sendMessage( "Type /bwaddon upgrade to upgrade file");
                    p.sendMessage(ChatColor.RED + "if you want to cancel the upgrade files do /bwaddon cancel");
                }
            }.runTaskLater(Hypixelify.getInstance(), 40L);
        }
    }

    @EventHandler
    public void onPlayerKilled(BedwarsPlayerKilledEvent e) {
        Game game = e.getGame();
        if (game != null && Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName()))
            Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).onPlayerKilled(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnd(BedwarsGameEndEvent e) {
        Game game = e.getGame();
        Hypixelify.getInstance().getArenaManager().removeArena(game.getName());
    }
    @EventHandler
    public void onOver(BedwarsGameEndingEvent e) {
        Game game = e.getGame();
        if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName()))
            Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).onOver(e);
    }

    @EventHandler
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e){
        Player player= e.getPlayer();
        Game game = e.getGame();
        String message = "&eThe game starts in &c{seconds} &eseconds";
        (new BukkitRunnable() {
            public void run() {
                if (player.isOnline()  && BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player) &&
                        game.getConnectedPlayers().contains(player)&&
                        game.getStatus().equals(GameStatus.WAITING)) {
                    if(game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                        String time = Main.getGame(game.getName()).getFormattedTimeLeft();
                        String[] units = time.split(":");
                        int seconds = Integer.parseInt(units[1]) + 1;
                        if (seconds < 2) {
                            player.sendMessage(translateColors(message.replace("{seconds}", String.valueOf(seconds)).replace("seconds", "second")));
                            player.sendTitle(translateColors("&c" + seconds),"",0,20,0);
                        } else if (seconds < 6) {
                            player.sendMessage(translateColors(message.replace("{seconds}", String.valueOf(seconds))));
                            player.sendTitle(translateColors("&c" + seconds),"",0,20,0);
                        }
                        else if(seconds < 11){
                            player.sendMessage(translateColors(message.replace("&c{seconds}", "&6" + seconds)));
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }).runTaskTimer(Hypixelify.getInstance(), 0L, 20L);
    }

    private  String translateColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }


}
