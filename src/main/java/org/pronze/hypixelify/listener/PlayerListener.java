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
import org.pronze.hypixelify.service.PlayerWrapperService;
import org.pronze.hypixelify.message.Messages;
import org.pronze.hypixelify.utils.SBAUtil;
import org.pronze.hypixelify.utils.Scheduler;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.CurrentTeam;
import org.screamingsandals.bedwars.game.GamePlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class PlayerListener extends AbstractListener {


    private final List<Material> allowed;
    private final List<Material> generatorDropItems;
    private final boolean partyEnabled, giveKillerResources, respawnCooldown, disableArmorInventoryMovement,
            disableArmorDamage, permanentItems, blockItemOnChest, blockItemDrops;

    private final int respawnTime;


    public PlayerListener() {
        blockItemDrops = SBAHypixelify.getConfigurator().config.getBoolean("block-item-drops", true);
        partyEnabled = SBAHypixelify.getConfigurator().config.getBoolean("party.enabled", true);
        giveKillerResources = SBAHypixelify.getConfigurator().config.getBoolean("give-killer-resources", true);
        respawnCooldown = Main.getConfigurator().config.getBoolean("respawn-cooldown.enabled");
        respawnTime = Main.getConfigurator().config.getInt("respawn-cooldown.time", 5);
        disableArmorInventoryMovement = SBAHypixelify.getConfigurator().config.getBoolean("disable-armor-inventory-movement", true);
        disableArmorDamage = SBAHypixelify.getConfigurator().config.getBoolean("disable-sword-armor-damage", true);
        permanentItems = SBAHypixelify.getConfigurator().config.getBoolean("permanent-items", true);
        blockItemOnChest = SBAHypixelify.getConfigurator().config.getBoolean("block-players-putting-certain-items-onto-chest", true);

        allowed = SBAUtil.parseMaterialFromConfig("allowed-item-drops");
        generatorDropItems = SBAUtil.parseMaterialFromConfig("running-generator-drops");
    }

    @Override
    public void onDisable() {
        allowed.clear();
        generatorDropItems.clear();
        HandlerList.unregisterAll(this);
    }




    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        final Player player = e.getEntity();

        if (!isInGame(player)) return;

        final Game game = BedwarsAPI.getInstance().getGameOfPlayer(player);
        if (game == null || game.getStatus() != GameStatus.RUNNING) return;

        final Arena arena = SBAHypixelify.getArena(game.getName());

        if (arena == null) return;

        Scheduler.runTaskLater(() -> {
            if (arena.getScoreBoard() != null) {
                arena.getScoreBoard().updateScoreboard();
            }
        }, 1L);

        final List<ItemStack> itemArr = new ArrayList<>();
        if (permanentItems) {
            ItemStack sword = Main.isLegacy() ? new ItemStack(Material.valueOf("WOOD_SWORD")) : new ItemStack(Material.WOODEN_SWORD);


            Arrays.stream(player.getInventory().getContents()).forEach(stack -> {
                if (stack == null) {
                    return;
                }
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
                Arrays.stream(player.getInventory().getContents()).forEach(drops->{
                    if(drops == null){
                        return;
                    }

                    if(generatorDropItems.contains(drops)){
                        killer.sendMessage("+" + drops.getAmount() + " " + drops.getType().name());
                        killer.getInventory().addItem(drops);
                    }
                });
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
        if (!blockItemDrops) return;

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

        if (typeName.contains("BOOTS")
                || typeName.contains("HELMET")
                || typeName.contains("LEGGINGS")
                || typeName.contains("CHESTPLATE")
                || typeName.contains("SWORD")) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (!partyEnabled) return;
        final Player player = e.getPlayer();
        final PlayerWrapperService dbManager = SBAHypixelify.getWrapperService();
        if (dbManager.getWrapper(player) == null) return;

        dbManager.handleOffline(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        SBAHypixelify.getWrapperService().register(player);

        if (!player.isOp())
            return;

        if (!SBAHypixelify.getConfigurator().config.getString("version", SBAHypixelify.getVersion())
                .contains(SBAHypixelify.getVersion())) {
            Scheduler.runTaskLater(() -> {
                player.sendMessage("§6[SBAHypixelify]: Plugin has detected a version change, do you want to upgrade internal files?");
                player.sendMessage("Type /bwaddon upgrade to upgrade file");
                player.sendMessage("§cif you want to cancel the upgrade files do /bwaddon cancel");
            }, 40L);
        }
    }


}
