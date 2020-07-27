package org.pronze.hypixelify.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.arena.Arena;
import org.pronze.hypixelify.database.PlayerDatabase;
import org.pronze.hypixelify.utils.ScoreboardUtil;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.CurrentTeam;
import org.screamingsandals.bedwars.game.GamePlayer;
import org.screamingsandals.bedwars.lib.nms.title.Title;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerListener implements Listener {
    static public HashMap<Player, List<ItemStack>> PlayerItems = new HashMap<>();
    static public HashMap<String, Integer> UpgradeKeys = new HashMap<>();
    static public ArrayList<Material> allowed = new ArrayList<>();
    static public ArrayList<Material> generatorDropItems = new ArrayList<>();

    public PlayerListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Hypixelify.getInstance());
        ShopUtil.initalizekeys();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent e) {

        Player player = e.getPlayer();

        if (!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)) return;

        Game game = BedwarsAPI.getInstance().getGameOfPlayer(player);
        if(!game.isPlayerInAnyTeam(player)) return;
        Team team = game.getTeamOfPlayer(player);

        new BukkitRunnable() {

            @Override
            public void run() {
                if (player.getGameMode().equals(GameMode.SURVIVAL) && BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)) {
                    ShopUtil.giveItemToPlayer(PlayerItems.get(player), player, team.getColor());
                    this.cancel();
                } else if (!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player))
                    this.cancel();
            }

        }.runTaskTimer(Hypixelify.getInstance(), 20L, 20L);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)) return;

        Game game = BedwarsAPI.getInstance().getGameOfPlayer(player);

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
                    newItem = ShopUtil.checkifUpgraded(newItem);
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

        if(Hypixelify.getConfigurator().config.getBoolean("give-killer-resources", true)) {
            Player killer = e.getEntity().getKiller();

            if (killer!= null && BedwarsAPI.getInstance().isPlayerPlayingAnyGame(killer) && killer.getGameMode().equals(GameMode.SURVIVAL)) {
                for (ItemStack dropItem : player.getInventory().getContents()) {
                    if (dropItem != null && generatorDropItems.contains(dropItem.getType())) {
                        killer.sendMessage("+" + dropItem.getAmount() + " " + dropItem.getType().name());
                        killer.getInventory().addItem(dropItem);
                    }
                }
            }
        }
        final Player victim = e.getEntity();
        GamePlayer gVictim = Main.getPlayerGameProfile(victim);

        CurrentTeam victimTeam = Main.getGame(game.getName()).getPlayerTeam(gVictim);
        if (Main.getConfigurator().config.getBoolean("respawn-cooldown.enabled") && victimTeam.isAlive() && game.isPlayerInAnyTeam(player) && game.getTeamOfPlayer(player).isTargetBlockExists()) {
            int respawnTime = Main.getConfigurator().config.getInt("respawn-cooldown.time", 5);

            new BukkitRunnable() {
                int livingTime = respawnTime;
                final GamePlayer gamePlayer = gVictim;
                final Player player = gamePlayer.player;

                @Override
                public void run() {
                    if (livingTime > 0) {
                        Title.sendTitle(player, Hypixelify.getConfigurator().config.getString("message.respawn-title"),
                                Hypixelify.getConfigurator().config.getString("message.respawn-subtitle").replace("%time%", String.valueOf(livingTime)), 0, 20, 0);
                        player.sendMessage(Hypixelify.getConfigurator().config.getString("message.respawn-subtitle").replace("%time%", String.valueOf(livingTime)));
                    }
                    livingTime--;
                    if (livingTime == 0) {
                        player.sendMessage(Hypixelify.getConfigurator().config.getString("message.respawned-title"));
                        Title.sendTitle(player, "§aRESPAWNED!", "", 5, 40, 5);
                        this.cancel();
                    }
                }
            }.runTaskTimer(Hypixelify.getInstance(), 0L, 20L);
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)) return;

        if (Hypixelify.getConfigurator().config.getBoolean("disable-armor-inventory-movement", true) && event.getSlotType() == SlotType.ARMOR)
            event.setCancelled(true);

        Inventory topSlot = event.getView().getTopInventory();
        Inventory bottomSlot = event.getView().getBottomInventory();
        if (event.getClickedInventory().equals(bottomSlot) && Hypixelify.getConfigurator().config.getBoolean("block-players-putting-certain-items-onto-chest", true) && (topSlot.getType() == InventoryType.CHEST || topSlot.getType() == InventoryType.ENDER_CHEST) && bottomSlot.getType() == InventoryType.PLAYER) {
            if (event.getCurrentItem().getType().name().endsWith("AXE") || event.getCurrentItem().getType().name().endsWith("SWORD")) {
                event.setResult(Event.Result.DENY);
                player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Hypixelify.getConfigurator().config.getString("message.cannot=put-item-on-chest"));
            }
        }
    }


    @EventHandler
    public void onItemDrop(PlayerDropItemEvent evt) {
        if (!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(evt.getPlayer())) return;

        if (!allowed.contains(evt.getItemDrop().getItemStack().getType()) && !evt.getItemDrop().getItemStack().getType().name().endsWith("WOOL")) {
            evt.setCancelled(true);
            evt.getPlayer().getInventory().remove(evt.getItemDrop().getItemStack());
        }
    }

    @EventHandler
    public void onPlayerLeave(BedwarsPlayerLeaveEvent e) {
        if (Main.isLegacy())
            return;

        Player player = e.getPlayer();
        ScoreboardUtil.removePlayer(player);
    }

    @EventHandler
    public void itemDamage(PlayerItemDamageEvent e) {
        Player player = e.getPlayer();
        if (!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)) return;
        if (!BedwarsAPI.getInstance().getGameOfPlayer(player).isPlayerInAnyTeam(player)) return;
        if (Main.getPlayerGameProfile(player).isSpectator) return;

        if (!Hypixelify.getConfigurator().config.getBoolean("disable-sword-armor-damage", true)) return;
        if (e.getItem().getType().toString().contains("BOOTS")
                || e.getItem().getType().toString().contains("HELMET")
                || e.getItem().getType().toString().contains("LEGGINGS")
                || e.getItem().getType().toString().contains("CHESTPLATE")
                || e.getItem().getType().toString().contains("SWORD")) {
            e.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStarted(BedwarsGameStartedEvent e) {
        final Game game = e.getGame();
        if(!Main.isLegacy()) {
            Map<Player, Scoreboard> scoreboards = ScoreboardUtil.getScoreboards();
            for (Player player : game.getConnectedPlayers()) {
                if (scoreboards.containsKey(player))
                    ScoreboardUtil.removePlayer(player);
            }
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
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (Hypixelify.getConfigurator().config.getBoolean("party.enabled", true) && Hypixelify.getInstance().playerData.get(p.getUniqueId()) == null)
            Hypixelify.getInstance().playerData.put(p.getUniqueId(), new PlayerDatabase(p));

        if (!p.isOp())
            return;

        if (!Hypixelify.getConfigurator().config.getString("version").contains(Hypixelify.getVersion())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.sendMessage(ChatColor.GOLD + "[SBAHypixelify]: Plugin has detected a version change, do you want to upgrade internal files?");
                    p.sendMessage("Type /bwaddon upgrade to upgrade file");
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
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e) {
        Player player = e.getPlayer();
        Game game = e.getGame();
        String message = "&eThe game starts in &c{seconds} &eseconds";
        (new BukkitRunnable() {
            public void run() {
                if (player.isOnline() && BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player) &&
                        game.getConnectedPlayers().contains(player) &&
                        game.getStatus().equals(GameStatus.WAITING)) {
                    if (game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                        String time = Main.getGame(game.getName()).getFormattedTimeLeft();
                        if (!time.contains("0-1")) {
                            String[] units = time.split(":");
                            int seconds = Integer.parseInt(units[1]) + 1;
                            if (seconds < 2) {
                                player.sendMessage(ShopUtil.translateColors(message.replace("{seconds}", String.valueOf(seconds)).replace("seconds", "second")));
                                player.sendTitle(ShopUtil.translateColors("&c" + seconds), "", 0, 20, 0);
                            } else if (seconds < 6) {
                                player.sendMessage(ShopUtil.translateColors(message.replace("{seconds}", String.valueOf(seconds))));
                                player.sendTitle(ShopUtil.translateColors("&c" + seconds), "", 0, 20, 0);
                            } else if (seconds % 10 == 0) {
                                player.sendMessage(ShopUtil.translateColors(message.replace("&c{seconds}", "&6" + seconds)));
                            }
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }).runTaskTimer(Hypixelify.getInstance(), 40L, 20L);
    }


}
