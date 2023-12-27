package io.github.pronze.sba.listener;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.Permissions;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.UpdateChecker;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.data.DegradableItem;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.GamePlayer;
import org.screamingsandals.lib.Server;
import org.screamingsandals.lib.player.Players;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.reflect.Reflect;

import io.github.pronze.lib.pronzelib.scoreboards.Scoreboard;
import io.github.pronze.lib.pronzelib.scoreboards.ScoreboardManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class PlayerListener implements Listener {
    private final List<Material> allowedDropItems = new ArrayList<>();
    private final List<Material> generatorDropItems = new ArrayList<>();

    @OnPostEnable
    public void registerListener() {
        if (SBA.isBroken())
            return;
        SBA.getInstance().registerListener(this);
        allowedDropItems.clear();
        generatorDropItems.clear();
        allowedDropItems.addAll(SBAUtil.parseMaterialFromConfig("allowed-item-drops"));
        generatorDropItems.addAll(SBAUtil.parseMaterialFromConfig("running-generator-drops"));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        final var player = e.getEntity();

        if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
            return;
        }

        final var game = Main.getInstance().getGameOfPlayer(player);
        if (game.getStatus() != GameStatus.RUNNING) {
            return;
        }

        final var arena = ArenaManager
                .getInstance()
                .get(game.getName())
                .orElseThrow();

        final var itemArr = new ArrayList<ItemStack>();
        final var sword = Main.isLegacy() ? new ItemStack(Material.valueOf("WOOD_SWORD"))
                : new ItemStack(Material.WOODEN_SWORD);

        Stream<ItemStack> stream;
        if (Server.isVersion(1, 9)) {
            stream = Arrays.stream(player.getInventory().getContents());
        } else {
            // getContents() work as get storage contents in 1.8, we need to manually append armor slots
            stream = Stream.concat(
                    Arrays.stream(player.getInventory().getContents()),
                    Arrays.stream(player.getInventory().getArmorContents())
            );
        }
        stream.filter(Objects::nonNull)
                .forEach(stack -> {
                    final String name = stack.getType().name();
                    var endStr = name.substring(name.contains("_") ? name.indexOf("_") + 1 : 0);
                    switch (endStr) {
                        case "SWORD":
                            itemArr.add(ShopUtil.downgradeItem(stack, DegradableItem.WEAPONARY));
                            break;
                        case "PICKAXE":
                        case "AXE":
                            itemArr.add(ShopUtil.downgradeItem(stack, DegradableItem.TOOLS));
                            break;
                        case "LEGGINGS":
                        case "BOOTS":
                        case "CHESTPLATE":
                        case "HELMET":
                            itemArr.add(ShopUtil.downgradeItem(stack, DegradableItem.ARMOR));
                            break;
                        case "SHEARS":
                            itemArr.add(stack);
                            break;
                    }
                });

        itemArr.add(sword);
        arena.getPlayerData(player.getUniqueId()).ifPresent(playerData -> playerData.setInventory(itemArr));

        if (SBAConfig.getInstance().getBoolean("give-killer-resources", true)) {
            final var killer = e.getEntity().getKiller();

            if (killer != null && Main.getInstance().isPlayerPlayingAnyGame(killer)
                    && killer.getGameMode() == GameMode.SURVIVAL) {
                Arrays.stream(player.getInventory().getContents())
                        .filter(Objects::nonNull)
                        .forEach(drop -> {
                            if (generatorDropItems.contains(drop.getType())) {
                                killer.sendMessage("+" + drop.getAmount() + " "
                                        + drop.getType().name().toLowerCase().replace("_", " "));
                                killer.getInventory().addItem(drop);
                            }
                        });
            }
        }

        final var gVictim = Main.getPlayerGameProfile(player);
        final var victimTeam = game.getTeamOfPlayer(player);

        if (victimTeam == null)
            return;
        if (SBAConfig.getInstance().getBoolean("respawn-cooldown.enabled", true) &&
                victimTeam.isAlive() && game.isPlayerInAnyTeam(player) &&
                game.getTeamOfPlayer(player).isTargetBlockExists()) {

            new BukkitRunnable() {
                final GamePlayer gamePlayer = gVictim;
                final Player player = gamePlayer.player;

                final SBAPlayerWrapper wrappedPlayer = Players.wrapPlayer(player).as(SBAPlayerWrapper.class);
                int livingTime = Main.getInstance().getConfig().getInt("respawn-cooldown.time", 5);
                byte buffer = 2;

                @Override
                public void run() {
                    if (!Main.isPlayerInGame(player)) {
                        this.cancel();
                        return;
                    }
                    final org.screamingsandals.lib.spectator.Component respawnTitle = LanguageService
                            .getInstance()
                            .get(MessageKeys.RESPAWN_COUNTDOWN_TITLE)
                            .replace("%time%", String.valueOf(livingTime))
                            .toComponent();
                    final org.screamingsandals.lib.spectator.Component respawnSubtitle = LanguageService
                            .getInstance()
                            .get(MessageKeys.RESPAWN_COUNTDOWN_SUBTITLE)
                            .replace("%time%", String.valueOf(livingTime))
                            .toComponent();
                    // send custom title because we disabled BedWars from showing any title
                    if (livingTime > 0) {
                        SBAUtil.sendTitle(wrappedPlayer, respawnTitle,
                                respawnSubtitle,
                                0, 20, 0);

                        LanguageService
                                .getInstance()
                                .get(MessageKeys.RESPAWN_COUNTDOWN_MESSAGE)
                                .replace("%time%", String.valueOf(livingTime))
                                .send(wrappedPlayer);
                        livingTime--;
                    }

                    if (livingTime <= 0) {
                        if (gVictim.isSpectator && buffer > 0) {
                            buffer--;
                        } else {
                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.RESPAWNED_MESSAGE)
                                    .send(wrappedPlayer);

                            var respawnedTitle = LanguageService
                                    .getInstance()
                                    .get(MessageKeys.RESPAWNED_TITLE)
                                    .toComponent();

                            SBAUtil.sendTitle(wrappedPlayer, respawnedTitle,
                                    org.screamingsandals.lib.spectator.Component.empty(),
                                    5, 40, 5);
                            ShopUtil.giveItemToPlayer(itemArr, player,
                                    Main.getInstance().getGameByName(game.getName()).getTeamOfPlayer(player)
                                            .getColor());
                            ShopUtil.applyTeamUpgrades(player, game);
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(SBA.getPluginInstance(), 0L, 20L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;

        if (!(event.getWhoClicked() instanceof Player))
            return;

        final var player = (Player) event.getWhoClicked();

        if (!Main.isPlayerInGame(player))
            return;

        if (SBAConfig.getInstance().getBoolean("disable-armor-inventory-movement", true) &&
                event.getSlotType() == SlotType.ARMOR)
            event.setCancelled(true);

        final var topSlot = event.getView().getTopInventory();
        final var bottomSlot = event.getView().getBottomInventory();
        final var clickedInventory = event.getClickedInventory();
        final var typeName = event.getCurrentItem().getType().name();

        if (clickedInventory == null)
            return;

        if (clickedInventory.equals(bottomSlot)
                && SBAConfig.getInstance().getBoolean("block-players-putting-certain-items-onto-chest", true)
                && (topSlot.getType() == InventoryType.CHEST || topSlot.getType() == InventoryType.ENDER_CHEST)
                && bottomSlot.getType() == InventoryType.PLAYER) {
            if (typeName.endsWith("AXE") || typeName.endsWith("SWORD")) {
                event.setResult(Event.Result.DENY);
                LanguageService
                        .getInstance()
                        .get(MessageKeys.CANNOT_PUT_ITEM_IN_CHEST)
                        .send(Players.wrapPlayer(player));
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent evt) {
        final var player = evt.getPlayer();

        if (!Main.isPlayerInGame(player))
            return;
        if (!SBAConfig.getInstance().getBoolean("block-item-drops", true))
            return;

        final var ItemDrop = evt.getItemDrop().getItemStack();
        final var type = ItemDrop.getType();

        if (!allowedDropItems.contains(type) && !type.name().endsWith("WOOL")) {
            evt.setCancelled(true);
            player.getInventory().remove(ItemDrop);
        }
    }

    @EventHandler
    public void itemDamage(PlayerItemDamageEvent event) {
        if (!Main.isPlayerInGame(event.getPlayer())) {
            return;
        }

        if (SBAConfig.getInstance().node("disable-item-damage").getBoolean(true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        final var player = e.getPlayer();
        final var uuid = player.getUniqueId();
        ScoreboardManager
                .getInstance()
                .fromCache(uuid)
                .ifPresent(Scoreboard::destroy);

        final var wrappedPlayer = Players.wrapPlayer(player)
                .as(SBAPlayerWrapper.class);
        SBA.getInstance()
                .getPartyManager()
                .getPartyOf(wrappedPlayer)
                .ifPresent(party -> {
                    party.removePlayer(wrappedPlayer);
                    if (party.getMembers().size() == 1) {
                        SBA.getInstance()
                                .getPartyManager()
                                .disband(party.getUUID());
                        return;
                    }
                    if (party.getPartyLeader().equals(wrappedPlayer)) {
                        party
                                .getMembers()
                                .stream()
                                .findAny()
                                .ifPresentOrElse(member -> {
                                    party.setPartyLeader(member);
                                    LanguageService
                                            .getInstance()
                                            .get(MessageKeys.PARTY_MESSAGE_PROMOTED_LEADER)
                                            .replace("%player%",
                                                    member.as(Player.class).getDisplayName() + ChatColor.RESET)
                                            .send(party.getMembers().toArray(new SBAPlayerWrapper[0]));

                                }, () -> SBA.getInstance().getPartyManager()
                                        .disband(party.getUUID()));
                    }
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_OFFLINE_LEFT)
                            .replace("%player%", player.getDisplayName() + ChatColor.RESET)
                            .send(party.getMembers().stream().filter(member -> !wrappedPlayer.equals(member))
                                    .toArray(SBAPlayerWrapper[]::new));
                });
        SBA.getInstance().getPlayerWrapperService().unregister(player);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        final var entity = event.getEntity();
        if (entity instanceof Player) {
            final var player = (Player) entity;

            if (Main.isPlayerInGame(player)) {
                final var game = Main.getInstance().getGameOfPlayer(player);
                ArenaManager
                        .getInstance()
                        .get(game.getName())
                        .ifPresent(arena -> arena.removeHiddenPlayer(player));

                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                    event.setDamage(SBAConfig.getInstance().node("explosion-damage").getDouble(1.0D));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        final var item = event.getItem();
        final var player = event.getPlayer();

        if (!Main.isPlayerInGame(player))
            return;

        if (item.getType() == Material.POTION) {
            final var potionMeta = (PotionMeta) item.getItemMeta();
            boolean isInvis = false;
            if (potionMeta.getBasePotionData().getType() == PotionType.INVISIBILITY) {
                isInvis = true;
            } else {
                if (potionMeta.hasCustomEffects()) {
                    isInvis = potionMeta
                            .getCustomEffects()
                            .stream()
                            .anyMatch(potionEffect -> potionEffect.getType().getName()
                                    .equalsIgnoreCase(PotionEffectType.INVISIBILITY.getName()));
                }
            }

            if (isInvis) {
                final var playerGame = Main.getInstance().getGameOfPlayer(player);
                ArenaManager
                        .getInstance()
                        .get(playerGame.getName())
                        .ifPresent(arena -> arena.addHiddenPlayer(player));
            }

            try {
                Reflect.setField(event.getClass(), "replacement", event, new ItemStack(Material.AIR));
                // event.setReplacement(new ItemStack(Material.AIR));
            } catch (Throwable t) {
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEquipt(PlayerItemHeldEvent event) {
        final var player = event.getPlayer();

        if (!Main.isPlayerInGame(player))
            return;

        final var playerGame = Main.getInstance().getGameOfPlayer(player);
        ArenaManager
                .getInstance()
                .get(playerGame.getName())
                .ifPresent(arena -> {
                    if (arena.isPlayerHidden(player)) {
                        arena.updateHiddenPlayer(player);
                    }
                });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractPlace(PlayerInteractEvent event) {
        if (!Main.isPlayerInGame(event.getPlayer()))
            return;
        if (event.isCancelled())
            return;
        if (!event.isBlockInHand())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (!event.hasBlock())
            return;

        BlockFace face = event.getBlockFace();
        Location loc = event.getClickedBlock().getRelative(face).getLocation();
        try {
            Collection<Entity> players = loc.getNearbyEntitiesByType(Player.class, 1.5, 1.5, 1.5, null);
            for (Entity playerEntity : players) {
                Player player = (Player) playerEntity;
                if (player.getGameMode() != GameMode.SURVIVAL) {
                    player.teleport(player.getLocation().add(0, 1.5, 0), TeleportCause.SPECTATE);
                }
            }
        } catch (Throwable t) {
            //Does not work on spigot
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractPlaceOnEntity(PlayerInteractEntityEvent event) {
        if (!Main.isPlayerInGame(event.getPlayer()))
            return;
        Logger.trace("onInteractPlaceOnEntity");
        if (event.isCancelled()) {
            Logger.trace("onInteractPlaceOnEntity.isCancelled");
            return;
        }
        Player player = event.getPlayer();
        if (player.getItemInHand() == null || !player.getItemInHand().getType().isBlock()) {
            Logger.trace("onInteractPlaceOnEntity.getItemInHand");
            return;
        }
        if (event.getRightClicked() instanceof Player) {
            Player target = (Player) event.getRightClicked();
            if (target.getGameMode() == GameMode.SURVIVAL) {
                Logger.trace("onInteractPlaceOnEntity.target-is-not-spectator");
                return;
            }
            Block replaced = target.getLocation().getBlock();
            BlockState state = replaced.getState();
            byte rawData = state.getRawData();

            BlockState newState = replaced.getState();

            newState.setType(player.getItemInHand().getType());
            // replaced.setType(player.getItemInHand().getType());
            Logger.trace("onInteractPlaceOnEntity {}", player.getItemInHand().getType());

            BlockPlaceEvent event_ = new BlockPlaceEvent(replaced, state, replaced, event.getPlayer().getItemInHand(),
                    player, true);

            Bukkit.getServer().getPluginManager().callEvent(event_);
            Logger.trace("onInteractPlaceOnEntity {} {}", event, event.isCancelled());

            if (event.isCancelled()) {
                Logger.trace("event.isCancelled");
                newState.setType(state.getType());
                newState.setRawData(rawData);
            }
            // replaced.getState().setType(player.getItemInHand().getType());
            newState.update(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final var player = e.getPlayer();
        SBA.getInstance().getPlayerWrapperService().register(player);

        if (player.hasPermission(Permissions.UPGRADE.getKey())) {
            if (SBA.getInstance().isPendingUpgrade()) {
                Bukkit.getScheduler().runTaskLater(SBA.getPluginInstance(), () -> {
                    player.sendMessage(
                            "§6[SBA]: Plugin has detected a version change, do you want to upgrade internal files?");
                    player.sendMessage("Type /sba upgrade to upgrade file");
                    player.sendMessage("§cif you want to cancel the upgrade files do /sba cancel");
                }, 40L);
            }
        }
        if (player.hasPermission(Permissions.UPDATE.getKey())) {
            {
                if (SBA.getInstance().isPendingUpdate() && SBAConfig.getInstance().shouldWarnPlayerAboutUpdate()) {

                    Bukkit.getScheduler().runTaskLater(SBA.getPluginInstance(), () -> {
                        UpdateChecker.getInstance().sendToUser(player);
                    }, 40L);
                }
            }
        }
    }
}