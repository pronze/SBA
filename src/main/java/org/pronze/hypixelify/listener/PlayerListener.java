package org.pronze.hypixelify.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.arena.Arena;
import org.pronze.hypixelify.manager.DatabaseManager;
import org.pronze.hypixelify.message.Messages;
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

import java.util.*;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class PlayerListener extends AbstractListener {
    static public HashMap<String, Integer> UpgradeKeys = new HashMap<>();
    static public ArrayList<Material> allowed = new ArrayList<>();
    static public ArrayList<Material> generatorDropItems = new ArrayList<>();
    private final boolean partyEnabled, giveKillerResources, respawnCooldown, disableArmorInventoryMovement,
            disableArmorDamage;

    private final int respawnTime;


    public PlayerListener() {
        ShopUtil.initalizekeys();
        partyEnabled = Hypixelify.getConfigurator().config.getBoolean("party.enabled", true);
        giveKillerResources = Hypixelify.getConfigurator().config.getBoolean("give-killer-resources", true);
        respawnCooldown = Main.getConfigurator().config.getBoolean("respawn-cooldown.enabled");
        respawnTime = Main.getConfigurator().config.getInt("respawn-cooldown.time", 5);
        disableArmorInventoryMovement = Hypixelify.getConfigurator().config.getBoolean("disable-armor-inventory-movement", true);
        disableArmorDamage = Hypixelify.getConfigurator().config.getBoolean("disable-sword-armor-damage", true);
    }

    @Override
    public void onDisable() {
        UpgradeKeys.clear();
        allowed.clear();
        generatorDropItems.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (!partyEnabled) return;
        Player player = e.getPlayer();
        final DatabaseManager dbManager = Hypixelify.getDatabaseManager();
        if (dbManager.getDatabase(player) == null) return;

        dbManager.handleOffline(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent e) {

        Player player = e.getPlayer();

        if (!isInGame(player)) return;

        if (getGame(player).getStatus() != GameStatus.RUNNING) return;

        Game game = BedwarsAPI.getInstance().getGameOfPlayer(player);
        if (!game.isPlayerInAnyTeam(player)) return;
        Team team = game.getTeamOfPlayer(player);
        List<ItemStack> playerItems = null;
        if (Hypixelify.getInstance().getArenaManager() == null || Hypixelify.getInstance().getArenaManager().getArenas() == null)
            return;

        if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName())) {
            Arena arena = Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName());
            playerItems = arena.getStorage().getItemsOfPlayer(player);
            if (playerItems == null) return;
        }

        List<ItemStack> finalPlayerItems = playerItems;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getGameMode().equals(GameMode.SURVIVAL) && isInGame(player)) {
                    if (finalPlayerItems != null)
                        ShopUtil.giveItemToPlayer(finalPlayerItems, player, team.getColor());
                    player.sendMessage(Messages.message_respawned_title);
                    sendTitle(player, "§aRESPAWNED!", "", 5, 40, 5);
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
        if (game.getStatus() != GameStatus.RUNNING) return;

        if (Hypixelify.getInstance().getArenaManager() != null
                && Hypixelify.getInstance().getArenaManager().getArenas() != null) {

            if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName())) {
                new BukkitRunnable() {
                    public void run() {
                        if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName())) {
                            if (Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).getScoreBoard() != null) {
                                Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).getScoreBoard().updateScoreboard();
                            }
                        }
                    }
                }.runTaskLater(Hypixelify.getInstance(), 1L);
            }
        }
        List<ItemStack> items = new ArrayList<>();
        ItemStack sword;
        if (Main.isLegacy())
            sword = new ItemStack(Material.valueOf("WOOD_SWORD"));
        else
            sword = new ItemStack(Material.WOODEN_SWORD);

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
        if (Hypixelify.getInstance().getArenaManager() != null
                && Hypixelify.getInstance().getArenaManager().getArenas() != null && Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName())) {
            Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).getStorage().putPlayerItems(player, items);
        }

        if (giveKillerResources) {
            Player killer = e.getEntity().getKiller();

            if (killer != null && isInGame(killer) && killer.getGameMode().equals(GameMode.SURVIVAL)) {
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
        if (respawnCooldown && victimTeam.isAlive() && game.isPlayerInAnyTeam(player) && game.getTeamOfPlayer(player).isTargetBlockExists()) {

            new BukkitRunnable() {
                final GamePlayer gamePlayer = gVictim;
                final Player player = gamePlayer.player;
                int livingTime = respawnTime;

                @Override
                public void run() {
                    if (livingTime > 0) {
                        sendTitle(player, Messages.message_respawn_title,
                                Messages.message_respawn_subtitle.replace("%time%", String.valueOf(livingTime)), 0, 20, 0);
                        player.sendMessage(Messages.message_respawn_subtitle.replace("%time%", String.valueOf(livingTime)));
                    }
                    livingTime--;
                    if (livingTime == 0) {
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

        if (!isInGame(player)) return;

        if (disableArmorInventoryMovement && event.getSlotType() == SlotType.ARMOR)
            event.setCancelled(true);

        Inventory topSlot = event.getView().getTopInventory();
        Inventory bottomSlot = event.getView().getBottomInventory();
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().equals(bottomSlot) && Hypixelify.getConfigurator().config.getBoolean("block-players-putting-certain-items-onto-chest", true) && (topSlot.getType() == InventoryType.CHEST || topSlot.getType() == InventoryType.ENDER_CHEST) && bottomSlot.getType() == InventoryType.PLAYER) {
            if (event.getCurrentItem().getType().name().endsWith("AXE") || event.getCurrentItem().getType().name().endsWith("SWORD")) {
                event.setResult(Event.Result.DENY);
                player.sendMessage("§c§l" + Hypixelify.getConfigurator().config.getString("message.cannot-put-item-on-chest"));
            }
        }
    }


    @EventHandler
    public void onItemDrop(PlayerDropItemEvent evt) {
        if (!isInGame(evt.getPlayer())) return;

        if (!allowed.contains(evt.getItemDrop().getItemStack().getType()) && !evt.getItemDrop().getItemStack().getType().name().endsWith("WOOL")) {
            evt.setCancelled(true);
            evt.getPlayer().getInventory().remove(evt.getItemDrop().getItemStack());
        }
    }

    @EventHandler
    public void onPlayerLeave(BedwarsPlayerLeaveEvent e) {
        Player player = e.getPlayer();
        ScoreboardUtil.removePlayer(player);

        //remove custom made objectives from player.
        if (Hypixelify.isProtocolLib() && player != null && player.isOnline()) {
            ProtocolManager m = ProtocolLibrary.getProtocolManager();
            try {
                PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
                packet.getIntegers().write(0, 1);
                packet.getStrings().write(0, "bwa-tag");
                m.sendServerPacket(player, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                PacketContainer packet = m.createPacket(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
                packet.getIntegers().write(0, 1);
                packet.getStrings().write(0, "bwa-tab");
                m.sendServerPacket(player, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Game game = e.getGame();
        if (game.getStatus() != GameStatus.RUNNING) return;
        if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName())) {
            if (Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).getScoreBoard() != null) {
                Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).getScoreBoard().updateScoreboard();
            }
        }


    }

    @EventHandler
    public void itemDamage(PlayerItemDamageEvent e) {
        if (!disableArmorDamage) return;
        Player player = e.getPlayer();
        if (!isInGame(player)) return;
        if (!BedwarsAPI.getInstance().getGameOfPlayer(player).isPlayerInAnyTeam(player)) return;
        if (Main.getPlayerGameProfile(player).isSpectator) return;


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
        Map<Player, Scoreboard> scoreboards = ScoreboardUtil.getScoreboards();
        for (Player player : game.getConnectedPlayers()) {
            if (scoreboards.containsKey(player))
                ScoreboardUtil.removePlayer(player);
        }
        Arena arena = new Arena(game);
        Hypixelify.getInstance().getArenaManager().addArena(game.getName(), arena);
        new BukkitRunnable() {
            public void run() {
                if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName()))
                    Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).getScoreBoard().updateScoreboard();
            }
        }.runTaskLater(Hypixelify.getInstance(), 2L);

        Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).onGameStarted(e);
    }

    @EventHandler
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {
        final Game game = e.getGame();
        if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName())) {
            Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).onTargetBlockDestroyed(e);
            new BukkitRunnable() {
                public void run() {
                    if (Hypixelify.getInstance().getArenaManager().getArenas().containsKey(game.getName())) {
                        if (Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).getScoreBoard() != null) {
                            Hypixelify.getInstance().getArenaManager().getArenas().get(game.getName()).getScoreBoard().updateScoreboard();
                        }
                    }
                }
            }.runTaskLater(Hypixelify.getInstance(), 1L);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (partyEnabled && Hypixelify.getDatabaseManager().getDatabase(p) == null)
            Hypixelify.getDatabaseManager().createDatabase(p);

        if (!p.isOp())
            return;

        if (!Objects.requireNonNull(Hypixelify.getConfigurator().config.getString("version")).contains(Hypixelify.getVersion())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.sendMessage("§6[SBAHypixelify]: Plugin has detected a version change, do you want to upgrade internal files?");
                    p.sendMessage("Type /bwaddon upgrade to upgrade file");
                    p.sendMessage("§cif you want to cancel the upgrade files do /bwaddon cancel");
                }
            }.runTaskLater(Hypixelify.getInstance(), 40L);
        }
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
        new BukkitRunnable() {
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
                                sendTitle(player, ShopUtil.translateColors("&c" + seconds), "", 0, 20, 0);
                            } else if (seconds < 6) {
                                player.sendMessage(ShopUtil.translateColors(message.replace("{seconds}", String.valueOf(seconds))));
                                sendTitle(player, ShopUtil.translateColors("&c" + seconds), "", 0, 20, 0);
                            } else if (seconds % 10 == 0) {
                                player.sendMessage(ShopUtil.translateColors(message.replace("&c{seconds}", "&6" + seconds)));
                            }
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Hypixelify.getInstance(), 40L, 20L);
    }


}
