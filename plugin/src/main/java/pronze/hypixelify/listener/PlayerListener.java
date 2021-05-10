package pronze.hypixelify.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.lib.lang.Lang;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.bedwars.player.PlayerManager;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.Permissions;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.game.ArenaManager;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.utils.Logger;
import pronze.lib.scoreboards.Scoreboard;
import pronze.lib.scoreboards.ScoreboardManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@AutoInitialize(listener = true)
public class PlayerListener implements Listener {
    private final List<Material> allowedDropItems;
    private final List<Material> generatorDropItems;

    public PlayerListener() {
        allowedDropItems = SBAUtil.parseMaterialFromConfig("allowed-item-drops");
        generatorDropItems = SBAUtil.parseMaterialFromConfig("running-generator-drops");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Logger.trace("Player death event called");
        final var player = e.getEntity();
        final var gameOptional = PlayerManager
                .getInstance()
                .getGameOfPlayer(player.getUniqueId());

        if (gameOptional.isEmpty()) return;

        final var game = gameOptional.get();
        if (game.getStatus() != GameStatus.RUNNING) return;

        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresentOrElse(arena -> {
                    final var itemArr = new ArrayList<ItemStack>();
                    final var sword = Main.isLegacy() ?
                            new ItemStack(Material.valueOf("WOOD_SWORD")) :
                            new ItemStack(Material.WOODEN_SWORD);

                    Arrays.stream(player
                            .getInventory()
                            .getContents()
                            .clone())
                            .filter(Objects::nonNull)
                            .forEach(stack -> {
                                final String name = stack.getType().name();
                                var endStr = name.substring(name.contains("_") ? name.indexOf("_") : 0);
                                switch (endStr) {
                                    case "SWORD":
                                        sword.addEnchantments(stack.getEnchantments());
                                        break;
                                    case "AXE":
                                        itemArr.add(ShopUtil.checkifUpgraded(stack));
                                        break;
                                    case "SHEARS":
                                    case "LEGGINGS":
                                    case "BOOTS":
                                    case "CHESTPLATE":
                                    case "HELMET":
                                        itemArr.add(stack);
                                        break;
                                }
                            });

                    itemArr.add(sword);
                    arena.getPlayerData(player.getUniqueId())
                            .ifPresent(playerData -> playerData.setInventory(itemArr));


                    if (SBAConfig.getInstance().getBoolean("give-killer-resources", true)) {
                        final var killer = e.getEntity().getKiller();

                        if (killer != null
                                && PlayerManager.getInstance().getGameOfPlayer(killer.getUniqueId()).isPresent()
                                && killer.getGameMode() == GameMode.SURVIVAL) {
                            Arrays.stream(player.getInventory().getContents())
                                    .filter(Objects::nonNull)
                                    .forEach(drop -> {
                                        if (generatorDropItems.contains(drop.getType())) {
                                            killer.sendMessage("+" + drop.getAmount() + " " + drop.getType().name());
                                            killer.getInventory().addItem(drop);
                                        }
                                    });
                        }
                    }

                    final var gVictim = PlayerManager
                            .getInstance()
                            .getPlayer(player.getUniqueId())
                            .orElseThrow();

                    final var victimTeam = game.getTeamOfPlayer(player);

                    if (SBAConfig.getInstance().getBoolean("respawn-cooldown.enabled", true) &&
                            victimTeam.isAlive() && game.isPlayerInAnyTeam(player) &&
                            game.getTeamOfPlayer(player).isTargetBlockExists()) {

                        new BukkitRunnable() {
                            final BedWarsPlayer gamePlayer = gVictim;
                            final Player player = gamePlayer.as(Player.class);
                            int livingTime = SBAConfig.getInstance().getInt("respawn-cooldown.time", 5);

                            byte buffer = 2;

                            String respawnTitle = LanguageService
                                    .getInstance()
                                    .get(MessageKeys.RESPAWN_COUNTDOWN_TITLE)
                                    .toString();

                            String respawnSubtitle = LanguageService
                                    .getInstance()
                                    .get(MessageKeys.RESPAWN_COUNTDOWN_SUBTITLE)
                                    .toString();
                            PlayerWrapper wrappedPlayer = PlayerMapper.wrapPlayer(player).as(PlayerWrapper.class);

                            @Override
                            public void run() {
                                if (!PlayerManager.getInstance().isPlayerInGame(player.getUniqueId())) {
                                    this.cancel();
                                    return;
                                }

                                //send custom title because we disabled BedWars from showing any title
                                if (livingTime > 0) {
                                    SBAUtil.sendTitle(wrappedPlayer, respawnTitle,
                                            respawnSubtitle.replace("%time%", String.valueOf(livingTime)),
                                            0, 20, 0);

                                    LanguageService
                                            .getInstance()
                                            .get(MessageKeys.RESPAWN_COUNTDOWN_MESSAGE)
                                            .replace("%time%", String.valueOf(livingTime))
                                            .send(wrappedPlayer);
                                    livingTime--;
                                }


                                if (livingTime == 0) {
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
                                                .toString();

                                        SBAUtil.sendTitle(wrappedPlayer, respawnedTitle, "",
                                                5, 40, 5);
                                        ShopUtil.giveItemToPlayer(itemArr, player,
                                                Main.getInstance().getGameManager().getGame(game.getName()).orElseThrow().getTeamOfPlayer(gamePlayer.as(Player.class)).getColor());
                                        this.cancel();
                                    }
                                }
                            }
                        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
                    }
                }, () -> Logger.trace("Event hit null arena"));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;

        if (!(event.getWhoClicked() instanceof Player)) return;

        final var player = (Player) event.getWhoClicked();

        if (!PlayerManager.getInstance().isPlayerInGame(player.getUniqueId())) return;

        if (SBAConfig.getInstance().getBoolean("disable-armor-inventory-movement", true) &&
                event.getSlotType() == SlotType.ARMOR)
            event.setCancelled(true);

        final var topSlot = event.getView().getTopInventory();
        final var bottomSlot = event.getView().getBottomInventory();
        final var clickedInventory = event.getClickedInventory();
        final var typeName = event.getCurrentItem().getType().name();

        if (clickedInventory == null) return;

        if (clickedInventory.equals(bottomSlot) && SBAConfig.getInstance().getBoolean("block-players-putting-certain-items-onto-chest", true)
                && (topSlot.getType() == InventoryType.CHEST || topSlot.getType() == InventoryType.ENDER_CHEST)
                && bottomSlot.getType() == InventoryType.PLAYER) {
            if (typeName.endsWith("AXE") || typeName.endsWith("SWORD")) {
                event.setResult(Event.Result.DENY);
                LanguageService
                        .getInstance()
                        .get(MessageKeys.CANNOT_PUT_ITEM_IN_CHEST)
                        .send(PlayerMapper.wrapPlayer(player));
            }
        }
    }


    @EventHandler
    public void onItemDrop(PlayerDropItemEvent evt) {
        final var player = evt.getPlayer();

        if (!PlayerManager.getInstance().isPlayerInGame(player.getUniqueId())) return;
        if (!SBAConfig.getInstance().getBoolean("block-item-drops", true)) return;

        final var ItemDrop = evt.getItemDrop().getItemStack();
        final var type = ItemDrop.getType();

        if (!allowedDropItems.contains(type) && !type.name().endsWith("WOOL")) {
            evt.setCancelled(true);
            player.getInventory().remove(ItemDrop);
        }
    }


    @EventHandler
    public void itemDamage(PlayerItemDamageEvent e) {
        if (!SBAConfig.getInstance().getBoolean("disable-sword-armor-damage", true)) return;
        var player = e.getPlayer();
        if (!PlayerManager.getInstance().isPlayerInGame(player.getUniqueId())) return;

        if (PlayerManager.getInstance().getPlayer(player.getUniqueId()).orElseThrow().isSpectator) return;

        final var typeName = e.getItem().getType().toString();
        final var afterUnderscore = typeName.substring(typeName.contains("_") ? typeName.indexOf("_") + 1 : 0);

        switch (afterUnderscore.toLowerCase()) {
            case "BOOTS":
            case "HELMET":
            case "CHESTPLATE":
            case "SWORD":
                e.setCancelled(true);
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
        final var wrappedPlayer = PlayerMapper.wrapPlayer(player)
                .as(PlayerWrapper.class);
        SBAHypixelify
                .getInstance()
                .getPartyManager()
                .getPartyOf(wrappedPlayer)
                .ifPresent(party -> {
                    party.removePlayer(wrappedPlayer);
                    if (party.getMembers().size() == 1) {
                        SBAHypixelify
                                .getInstance()
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
                                            .replace("%player%", member.getName())
                                            .send(party.getMembers().toArray(new PlayerWrapper[0]));

                                }, () -> SBAHypixelify.getInstance().getPartyManager()
                                        .disband(party.getUUID()));
                    }
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_OFFLINE_LEFT)
                            .replace("%player%", player.getName())
                            .send(party.getMembers().stream().filter(member -> !wrappedPlayer.equals(member)).toArray(PlayerWrapper[]::new));
                });
        SBAHypixelify.getInstance().getPlayerWrapperService().unregister(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        final var entity = event.getEntity();
        if (entity instanceof Player) {
            final var player = (Player) entity;
            PlayerManager
                    .getInstance()
                    .getGameOfPlayer(player.getUniqueId())
                    .flatMap(game -> ArenaManager
                            .getInstance()
                            .get(game.getName())).ifPresent(arena -> arena.removeHiddenPlayer(player));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        final var item = event.getItem();
        final var player = event.getPlayer();

        if (!PlayerManager.getInstance().isPlayerInGame(player.getUniqueId())) return;

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
                            .anyMatch(potionEffect -> potionEffect.getType() == PotionEffectType.INVISIBILITY);
                }
            }
            

            if (isInvis) {
                final var playerGame = PlayerManager
                        .getInstance()
                        .getGameOfPlayer(player.getUniqueId())
                        .orElseThrow();

                ArenaManager
                        .getInstance()
                        .get(playerGame.getName())
                        .ifPresent(arena -> arena.addHiddenPlayer(player));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final var player = e.getPlayer();
        SBAHypixelify.getInstance().getPlayerWrapperService().register(player);
        if (player.hasPermission(Permissions.UPGRADE.getKey())) {
            if (SBAHypixelify.getInstance().isPendingUpgrade()) {
                Bukkit.getScheduler().runTaskLater(SBAHypixelify.getInstance(), () -> {
                    player.sendMessage("§6[SBAHypixelify]: Plugin has detected a version change, do you want to upgrade internal files?");
                    player.sendMessage("Type /bwaddon upgrade to upgrade file");
                    player.sendMessage("§cif you want to cancel the upgrade files do /bwaddon cancel");
                }, 40L);
            }
        }
    }


}
