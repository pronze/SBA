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
import org.screamingsandals.bedwars.game.GamePlayer;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.Permissions;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.game.ArenaManager;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.scoreboards.Scoreboard;
import pronze.lib.scoreboards.ScoreboardManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Service
public class PlayerListener implements Listener {
    private final List<Material> allowedDropItems;
    private final List<Material> generatorDropItems;

    public PlayerListener() {
        allowedDropItems = SBAUtil.parseMaterialFromConfig("allowed-item-drops");
        generatorDropItems = SBAUtil.parseMaterialFromConfig("running-generator-drops");
    }

    @OnPostEnable
    public void registerListener() {
        SBAHypixelify.getInstance().registerListener(this);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        final var player = e.getEntity();

        if (!Main.getInstance().isPlayerPlayingAnyGame(player)) return;

        final var game = Main.getInstance().getGameOfPlayer(player);
        if (game.getStatus() != GameStatus.RUNNING) return;

        final var optionalArena = ArenaManager.getInstance().get(game.getName());

        if (optionalArena.isEmpty()) {
            return;
        }

        final var arena = optionalArena.get();

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
                    && Main.getInstance().isPlayerPlayingAnyGame(killer)
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

        final var gVictim = Main.getPlayerGameProfile(player);

        final var victimTeam = game.getTeamOfPlayer(player);

        if (SBAConfig.getInstance().getBoolean("respawn-cooldown.enabled", true) &&
                victimTeam.isAlive() && game.isPlayerInAnyTeam(player) &&
                game.getTeamOfPlayer(player).isTargetBlockExists()) {

            new BukkitRunnable() {
                final GamePlayer gamePlayer = gVictim;
                final Player player = gamePlayer.player;
                int livingTime = SBAConfig.getInstance().getInt("respawn-cooldown.time", 5);

                byte buffer = 2;

                final String respawnTitle = LanguageService
                        .getInstance()
                        .get(MessageKeys.RESPAWN_COUNTDOWN_TITLE)
                        .toString();

                final String respawnSubtitle = LanguageService
                        .getInstance()
                        .get(MessageKeys.RESPAWN_COUNTDOWN_SUBTITLE)
                        .toString();
                final PlayerWrapper wrappedPlayer = PlayerMapper.wrapPlayer(player).as(PlayerWrapper.class);

                @Override
                public void run() {
                    if (!Main.isPlayerInGame(player)) {
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
                                    Main.getInstance().getGameByName(game.getName()).getTeamOfPlayer(player).getColor());
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(SBAHypixelify.getPluginInstance(), 0L, 20L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;

        if (!(event.getWhoClicked() instanceof Player)) return;

        final var player = (Player) event.getWhoClicked();

        if (!Main.isPlayerInGame(player)) return;

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

        if (!Main.isPlayerInGame(player)) return;
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
        if (!Main.isPlayerInGame(player)) return;

        if (Main.getPlayerGameProfile(player).isSpectator) return;

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
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        final var item = event.getItem();
        final var player = event.getPlayer();

        if (!Main.isPlayerInGame(player)) return;

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
                final var playerGame = Main.getInstance().getGameOfPlayer(player);
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
                Bukkit.getScheduler().runTaskLater(SBAHypixelify.getPluginInstance(), () -> {
                    player.sendMessage("§6[SBAHypixelify]: Plugin has detected a version change, do you want to upgrade internal files?");
                    player.sendMessage("Type /bwaddon upgrade to upgrade file");
                    player.sendMessage("§cif you want to cancel the upgrade files do /bwaddon cancel");
                }, 40L);
            }
        }
    }


}
