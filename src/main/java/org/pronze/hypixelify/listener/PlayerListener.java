package org.pronze.hypixelify.listener;

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
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.database.GameStorage;
import org.pronze.hypixelify.manager.DatabaseManager;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.CurrentTeam;
import org.screamingsandals.bedwars.game.GamePlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (!isInGame(player)) return;

        Game game = BedwarsAPI.getInstance().getGameOfPlayer(player);
        if (game == null || game.getStatus() != GameStatus.RUNNING) return;


        if (Hypixelify.getArenaManager().getArenas().containsKey(game.getName())) {
            new BukkitRunnable() {
                public void run() {
                    if (Hypixelify.getArenaManager().getArenas().containsKey(game.getName())) {
                        if (Hypixelify.getArenaManager().getArenas().get(game.getName()).getScoreBoard() != null) {
                            Hypixelify.getArenaManager().getArenas().get(game.getName()).getScoreBoard().updateScoreboard();
                        }
                    }
                }
            }.runTaskLater(Hypixelify.getInstance(), 1L);
        }

        List<ItemStack> itemArr = new ArrayList<>();
        GameStorage gameStorage = null;
        if (Hypixelify.getArenaManager().getArenas().containsKey(game.getName()))
            gameStorage = Hypixelify.getArenaManager().getArenas().get(game.getName()).getStorage();

        if (gameStorage != null) {
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
                        itemArr.add(newItem);
                    } else if (newItem.getType().name().contains("LEGGINGS") ||
                            newItem.getType().name().contains("BOOTS") ||
                            newItem.getType().name().contains("CHESTPLATE") ||
                            newItem.getType().name().contains("HELMET"))
                        itemArr.add(newItem);
                }
            }

            itemArr.add(sword);
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
        if (respawnCooldown && victimTeam.isAlive() && game.isPlayerInAnyTeam(player) &&
                game.getTeamOfPlayer(player).isTargetBlockExists()) {

            final List<ItemStack> playerItems = itemArr;
            new BukkitRunnable() {
                final GamePlayer gamePlayer = gVictim;
                final Player player = gamePlayer.player;
                int livingTime = respawnTime;
                int waitTimeout = 4;

                @Override
                public void run() {
                    if (livingTime > 0) {
                        sendTitle(player, Messages.message_respawn_title,
                                Messages.message_respawn_subtitle.replace("%time%", String.valueOf(livingTime)), 0, 20, 0);
                        player.sendMessage(Messages.message_respawn_subtitle.replace("%time%", String.valueOf(livingTime)));
                        livingTime--;
                    }
                    if (livingTime == 0) {
                        if(waitTimeout > 0 && isInGame(player)
                                && player.getGameMode() == GameMode.SPECTATOR){
                            waitTimeout--;
                        } else {
                            if (!isInGame(player) || player.getGameMode() != GameMode.SURVIVAL
                             || game.getStatus() != GameStatus.RUNNING) {
                                cancel();
                            } else {
                                player.sendMessage(Messages.message_respawned_title);
                                sendTitle(player, "§aRESPAWNED!", "", 5, 40, 5);
                                ShopUtil.giveItemToPlayer(playerItems, player, victimTeam.getColor());
                                this.cancel();
                            }
                        }
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


}
