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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.arena.Arena;
import org.pronze.hypixelify.manager.DatabaseManager;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.utils.Scheduler;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.CurrentTeam;
import org.screamingsandals.bedwars.game.GamePlayer;

import java.util.*;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class PlayerListener extends AbstractListener {

     private final ArrayList<Material> allowed = new ArrayList<>();
     private final ArrayList<Material> generatorDropItems = new ArrayList<>();
     private final boolean partyEnabled, giveKillerResources, respawnCooldown, disableArmorInventoryMovement,
            disableArmorDamage, permanentItems, blockItemOnChest;

    private final int respawnTime;


    public PlayerListener() {
        partyEnabled = SBAHypixelify.getConfigurator().config.getBoolean("party.enabled", true);
        giveKillerResources = SBAHypixelify.getConfigurator().config.getBoolean("give-killer-resources", true);
        respawnCooldown = Main.getConfigurator().config.getBoolean("respawn-cooldown.enabled");
        respawnTime = Main.getConfigurator().config.getInt("respawn-cooldown.time", 5);
        disableArmorInventoryMovement = SBAHypixelify.getConfigurator().config.getBoolean("disable-armor-inventory-movement", true);
        disableArmorDamage = SBAHypixelify.getConfigurator().config.getBoolean("disable-sword-armor-damage", true);
        permanentItems  = SBAHypixelify.getConfigurator().config.getBoolean("permanent-items", true);
        blockItemOnChest  =  SBAHypixelify.getConfigurator().config.getBoolean("block-players-putting-certain-items-onto-chest", true);



        SBAHypixelify.getConfigurator().config.getStringList("allowed-item-drops").forEach(material ->{
            Material mat;
            try {
                mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
            } catch (Exception ignored) {
                return;
            }
            allowed.add(mat);
        });

        SBAHypixelify.getConfigurator().config.getStringList("running-generator-drops").forEach(material -> {
            Material mat;
            try {
                mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
            } catch (Exception ignored) {
                return;
            }
            generatorDropItems.add(mat);
        });
    }

    @Override
    public void onDisable() {
        allowed.clear();
        generatorDropItems.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (!partyEnabled) return;
        final Player player = e.getPlayer();
        final DatabaseManager dbManager = SBAHypixelify.getDatabaseManager();
        if (dbManager.getDatabase(player) == null) return;

        dbManager.handleOffline(player);
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        final Player player = e.getEntity();

        if (!isInGame(player)) return;

        final Game game = BedwarsAPI.getInstance().getGameOfPlayer(player);
        if (game == null || game.getStatus() != GameStatus.RUNNING) return;

        final Arena arena = SBAHypixelify.getArena(game.getName());

        if(arena == null) return;

        Scheduler.runTaskLater(() ->{
            if(arena.getScoreBoard() != null){
                arena.getScoreBoard().updateScoreboard();
            }
        }, 1L);

        final List<ItemStack> itemArr = new ArrayList<>();
        if(permanentItems) {
            ItemStack sword = Main.isLegacy() ? new ItemStack(Material.valueOf("WOOD_SWORD")) : new ItemStack(Material.WOODEN_SWORD);


            Arrays.stream(player.getInventory().getContents()).forEach(stack -> {
                final String name = stack.getType().name();

                if (name.endsWith("SWORD"))
                    sword.addEnchantments(stack.getEnchantments());

                if (name.endsWith("AXE"))
                    itemArr.add(ShopUtil.checkifUpgraded(stack));

                if (name.endsWith("LEGGINGS") ||
                        name.endsWith("BOOTS") ||
                        name.endsWith("CHESTPLATE") ||
                        name.endsWith("HELMET"))
                    itemArr.add(stack);

            });

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
                        if (waitTimeout > 0 && isInGame(player)
                                && player.getGameMode() == GameMode.SPECTATOR) {
                            waitTimeout--;
                        } else {
                            if (!isInGame(player) || player.getGameMode() != GameMode.SURVIVAL
                                    || game.getStatus() != GameStatus.RUNNING) {
                                cancel();
                            } else {
                                player.sendMessage(Messages.message_respawned_title);
                                sendTitle(player, "§aRESPAWNED!", "", 5, 40, 5);
                                ShopUtil.giveItemToPlayer(itemArr, player, victimTeam.getColor());
                                this.cancel();
                            }
                        }
                    }
                }
            }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
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

        final Inventory topSlot = event.getView().getTopInventory();
        final Inventory bottomSlot = event.getView().getBottomInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final String typeName = event.getCurrentItem().getType().name();

        if (clickedInventory == null) return;

        if (clickedInventory.equals(bottomSlot) && blockItemOnChest &&
                (topSlot.getType() == InventoryType.CHEST || topSlot.getType() == InventoryType.ENDER_CHEST) &&
                bottomSlot.getType() == InventoryType.PLAYER) {
            if (typeName.endsWith("AXE") || typeName.endsWith("SWORD")) {
                event.setResult(Event.Result.DENY);
                player.sendMessage("§c§l" + SBAHypixelify.getConfigurator().config.getString("message.cannot-put-item-on-chest"));
            }
        }
    }


    @EventHandler
    public void onItemDrop(PlayerDropItemEvent evt) {
        if (!isInGame(evt.getPlayer())) return;

        final Player player = evt.getPlayer();
        final ItemStack ItemDrop = evt.getItemDrop().getItemStack();
        final Material type = ItemDrop.getType();

        if (!allowed.contains(type) && !type.name().endsWith("WOOL")) {
            evt.setCancelled(true);
            player.getInventory().remove(ItemDrop);
        }
    }


    @EventHandler
    public void itemDamage(PlayerItemDamageEvent e) {
        if (!disableArmorDamage) return;
        Player player = e.getPlayer();
        if (!isInGame(player)) return;
        if (!BedwarsAPI.getInstance().getGameOfPlayer(player).isPlayerInAnyTeam(player)) return;
        if (Main.getPlayerGameProfile(player).isSpectator) return;

        final String typeName = e.getItem().getType().toString();

        if (       typeName.contains("BOOTS")
                || typeName.contains("HELMET")
                || typeName.contains("LEGGINGS")
                || typeName.contains("CHESTPLATE")
                || typeName.contains("SWORD")) {
            e.setCancelled(true);
        }

    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (partyEnabled && SBAHypixelify.getDatabaseManager().getDatabase(p) == null)
            SBAHypixelify.getDatabaseManager().createDatabase(p);

        if (!p.isOp())
            return;

        if (!SBAHypixelify.getConfigurator().config.getString("version", SBAHypixelify.getVersion())
                .contains(SBAHypixelify.getVersion())) {
            Scheduler.runTaskLater(()->{
                p.sendMessage("§6[SBAHypixelify]: Plugin has detected a version change, do you want to upgrade internal files?");
                p.sendMessage("Type /bwaddon upgrade to upgrade file");
                p.sendMessage("§cif you want to cancel the upgrade files do /bwaddon cancel");
            }, 40L);
        }
    }


}
