package io.github.pronze.sba.listener;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.events.SBAFinalKillEvent;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.party.IParty;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.utils.MiscUtils;
import org.screamingsandals.lib.player.Players;
import org.screamingsandals.lib.spectator.Component;
import org.screamingsandals.lib.tasker.DefaultThreads;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.game.Arena;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.inventories.PlayerTrackerInventory;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.lib.pronzelib.scoreboards.Scoreboard;
import io.github.pronze.lib.pronzelib.scoreboards.ScoreboardManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.screamingsandals.bedwars.lib.lang.I18n.i18nonly;

@Service
public class BedWarsListener implements Listener {
    private final Map<UUID, BukkitTask> runnableCache = new HashMap<>();
    private final Map<UUID, ItemStack[]> inventoryContent = new HashMap<>();
    private final Map<UUID, Boolean> collidableValue = new HashMap<>();

    @OnPostEnable
    public void registerListener() {
        if(SBA.isBroken())return;
        SBA.getInstance().registerListener(this);
    }

    @EventHandler
    public void onStarted(BedwarsGameStartedEvent e) {
        Logger.trace("SBA onStarted{}", e);

        final var game = e.getGame();
        final var arena = ArenaManager
                .getInstance()
                .createArena(game);

        ((Arena) arena).onGameStarted();
    }

    @EventHandler
    public void onBwReload(PluginEnableEvent event) {
        final var pluginName = event.getPlugin().getName();
        if (pluginName.equalsIgnoreCase(Main.getInstance().getName())) {
            if (!SBA.getPluginInstance().isEnabled()) {
                return;
            }
            Logger.trace("Re registering listeners!");
            final var listeners = SBA.getInstance().getRegisteredListeners();

            listeners.forEach(SBA.getInstance()::unregisterListener);
            listeners.forEach(SBA.getInstance()::registerListener);

            Logger.trace("Registration complete!");
        }
    }

    @EventHandler
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent e) {

        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> ((Arena) arena).onTargetBlockDestroyed(e));
    }

    @EventHandler
    public void onPostRebuildingEvent(BedwarsPostRebuildingEvent e) {

        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> ((Arena) arena).onOver(e));
        ArenaManager
                .getInstance()
                .removeArena(game);
    }

    @EventHandler
    public void onGameTick(BedwarsGameTickEvent e) {
        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> ((Arena) arena).onGameTick(e));
    }

    @EventHandler
    public void onOver(BedwarsGameEndingEvent e) {
        Logger.trace("SBA onOver{}", e);

        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> ((Arena) arena).onOver(e));
    }

    public io.github.pronze.sba.party.PartySetting.GameMode gamemodeOf(Player connectedPlayer) {
        AtomicReference<io.github.pronze.sba.party.PartySetting.GameMode> ref = new AtomicReference<>(
                io.github.pronze.sba.party.PartySetting.GameMode.PUBLIC);
        SBA.getInstance().getPartyManager().getPartyOf(SBA.getInstance().getPlayerWrapper(connectedPlayer))
                .ifPresent(party -> {
                    ref.set(party.getSettings().getGamemode());
                });
        return ref.get();
    }

    @EventHandler
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e) {
        Logger.trace("SBA onBWLobbyJoin{}", e);

        final var player = e.getPlayer();
        final var wrappedPlayer = SBA.getInstance().getPlayerWrapper((player));
        final var task = runnableCache.get(player.getUniqueId());
        final var game = (Game) e.getGame();
        if (task != null) {
            SBAUtil.cancelTask(task);
        }
        AtomicBoolean isCancelled = new AtomicBoolean();
        isCancelled.set(false);
        Optional<IParty> maybeParty = SBA
                .getInstance()
                .getPartyManager()
                .getPartyOf(wrappedPlayer);

        maybeParty
                .ifPresentOrElse(party -> {
                    // Party of the player that joined
                    Logger.trace("Player {} has a party ", player);
                    if (party.getSettings()
                            .getGamemode() == io.github.pronze.sba.party.PartySetting.GameMode.PRIVATE) {
                        Logger.trace("Player {} party is private ", player);
                        game.getConnectedPlayers().forEach(connectedPlayer -> {
                            if (connectedPlayer == player)
                                return;
                            if (!party.getMembers().contains(SBA.getInstance().getPlayerWrapper(connectedPlayer))) {
                                isCancelled.set(true);
                                Logger.trace(
                                        "Preventing join as someone from outside the party is already in the game: {}",
                                        connectedPlayer);

                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.MESSAGE_ARENA_BUSY)
                                        .replace("%game%", game.getName())
                                        .send(wrappedPlayer);
                                // Prevent joining
                                return;
                            }
                        });
                    } else {
                        Logger.trace("Player {} party is public ", player);

                        game.getConnectedPlayers().forEach(connectedPlayer -> {
                            if (connectedPlayer == player)
                                return;
                            if (gamemodeOf(
                                    connectedPlayer) == io.github.pronze.sba.party.PartySetting.GameMode.PRIVATE) {
                                isCancelled.set(true);
                                Logger.trace("Preventing join due as a private party is in the lobby");
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.MESSAGE_ARENA_BUSY)
                                        .replace("%game%", game.getName())
                                        .send(wrappedPlayer);
                                return;
                            }
                        });
                    }
                    if (!wrappedPlayer.equals(party.getPartyLeader())) {
                        if (game != Main.getInstance().getGameOfPlayer(party.getPartyLeader().getInstance())) {
                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                    .send(wrappedPlayer);

                            isCancelled.set(true);
                            return;
                        }
                    }

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_WARP)
                            .send(wrappedPlayer);
                }, () -> {
                    game.getConnectedPlayers().forEach(connectedPlayer -> {
                        if (gamemodeOf(
                                connectedPlayer) == io.github.pronze.sba.party.PartySetting.GameMode.PRIVATE) {
                            Logger.trace("Preventing join due as a private party is in the lobby");
                            isCancelled.set(true);
                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.MESSAGE_ARENA_BUSY)
                                    .replace("%game%", game.getName())
                                    .send(wrappedPlayer);
                            return;
                        }
                    });
                });
        if (!isCancelled.get()) {
            if (SBAConfig.getInstance().party().autojoin())

                maybeParty.ifPresent(party -> party.getMembers()
                        .stream().filter(member -> !wrappedPlayer.equals(member))
                        .forEach(member -> {
                            final var memberGame = Main.getInstance().getGameOfPlayer(member.getInstance());

                            Bukkit.getScheduler().runTask(SBA.getPluginInstance(), () -> {
                                if (game != memberGame) {
                                    if (memberGame != null)
                                        memberGame.leaveFromGame(member.getInstance());
                                    game.joinToGame(member.getInstance());
                                    LanguageService
                                            .getInstance()
                                            .get(MessageKeys.PARTY_MESSAGE_WARP)
                                            .send(member);
                                }
                            });
                        }));
        } else {
            game.leaveFromGame(player);

            maybeParty.ifPresent(party -> {
                LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                        .send(wrappedPlayer);
            });
            return;
        }

        switch (game.getStatus()) {
            case WAITING:
                var bukkitTask = new BukkitRunnable() {
                    int buffer = 1; // fixes the bug where it constantly shows will start in 1 second

                    @Override
                    public void run() {
                        if (game.getStatus() == GameStatus.WAITING) {
                            if (game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                                String time = game.getFormattedTimeLeft();
                                if (!time.contains("0-1")) {
                                    String[] units = time.split(":");
                                    int seconds = Integer.parseInt(units[1]) + 1 + Integer.parseInt(units[0]) * 60;
                                    if (buffer == seconds)
                                        return;
                                    buffer = seconds;
                                    if (seconds <= 10) {
                                        var message = LanguageService
                                                .getInstance()
                                                .get(MessageKeys.GAME_STARTS_IN_MESSAGE)
                                                .replace("%seconds%",
                                                        seconds <= 60 ? String.valueOf(seconds)
                                                                : game.getFormattedTimeLeft())
                                                .toString();

                                        message = seconds == 1 ? message
                                                .replace("seconds", "second") : message;
                                        player.sendMessage(message);
                                        SBAUtil.sendTitle(Players.wrapPlayer(player),
                                                Component.fromLegacy(ShopUtil.translateColors("&c" + seconds)),
                                                org.screamingsandals.lib.spectator.Component.empty(), 0, 20, 0);
                                    }
                                }
                            }
                        } else {
                            this.cancel();
                            runnableCache.remove(player.getUniqueId());
                        }
                    }
                }.runTaskTimer(SBA.getPluginInstance(), 3L, 20L);
                runnableCache.put(player.getUniqueId(), bukkitTask);
                break;
            case RUNNING:
                final var arena = ArenaManager
                        .getInstance()
                        .get(game.getName())
                        .orElseThrow();
                if (SBAConfig.getInstance().getBoolean("game-scoreboard.enabled", true))
                    arena.getScoreboardManager().createScoreboard(player);
                ((Arena) arena).getRotatingGenerators().forEach(generator -> {
                    generator.addViewer(player);
                });
                break;
        }
    }

    @EventHandler
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent e) {
        Logger.trace("SBA EonBedWarsPlayerLeave{}", e);
        final var player = e.getPlayer();
        final var task = runnableCache.get(player.getUniqueId());
        final var game = e.getGame();
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> {
                    arena.removeVisualsForPlayer(player);
                    arena.removePlayerFromGame(player);
                });

        if (task != null) {
            SBAUtil.cancelTask(task);
        }
        runnableCache.remove(player.getUniqueId());

        ScoreboardManager
                .getInstance()
                .fromCache(player.getUniqueId())
                .ifPresent(Scoreboard::destroy);

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        var maybeParty = PartyManager.getInstance().getPartyOf(SBA.getInstance().getPlayerWrapper(e.getPlayer()));
        if (SBAConfig.getInstance().party().autojoin())
            maybeParty.ifPresent(party -> {
                if (party.getPartyLeader().getInstance() == e.getPlayer()) {
                    party.getMembers().forEach(member -> {
                        if (member != e.getPlayer()) {
                            Tasker.run(DefaultThreads.GLOBAL_THREAD, () -> {
                                var memberGame = Main.getInstance().getGameOfPlayer(member.getInstance());
                                if (memberGame != null) {
                                    memberGame.leaveFromGame(member.getInstance());
                                }
                            });
                        }
                    });
                }
            });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerRespawnEvent(PlayerRespawnEvent e) {
        final var victim = e.getPlayer();

        if (!Main.isPlayerInGame(victim)) {
            return;
        }
        final var game = Main.getInstance().getGameOfPlayer(victim);
        // query arena instance for access to Victim/Killer data
        Tasker.run(DefaultThreads.GLOBAL_THREAD, () -> {
            ArenaManager
                    .getInstance()
                    .get(game.getName())
                    .ifPresent(arena -> {
                        arena.addVisualsForPlayer(victim);
                    });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedwarsPlayerPlay(PlayerGameModeChangeEvent e) {
        final var player = e.getPlayer();

        if (!Main.isPlayerInGame(player)) {
            return;
        }
        if (player.getGameMode() == GameMode.SURVIVAL) {
            return;
        }
        if (e.getNewGameMode() != GameMode.SURVIVAL) {
            return;
        }
        if (player.getGameMode() == GameMode.ADVENTURE) {
            player.getInventory().remove(SBAConfig.getInstance().spectator().teleporter().get());
            player.getInventory().remove(SBAConfig.getInstance().spectator().leave().get());
            if(!SBAConfig.getInstance().spectator().tracker().keepOnStart())
            {
                player.getInventory().remove(SBAConfig.getInstance().spectator().tracker().get());
            }
        }
        if (SBAConfig.getInstance().spectator().adventure()) {
            player.closeInventory();
            player.setFlying(false);
            player.setAllowFlight(false);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            final var game = Main.getInstance().getGameOfPlayer(player);
            final var arena = ArenaManager
                    .getInstance().get(game.getName());
            arena.ifPresent(arena_ -> {
                arena_.removeHiddenPlayer(player);
            });
        }
        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
            if (inventoryContent.containsKey(player.getUniqueId())) {
                player.getInventory().clear();
                player.getInventory().setContents(inventoryContent.get(player.getUniqueId()));
                inventoryContent.remove(player.getUniqueId());
            }
            if (collidableValue.containsKey(player.getUniqueId())) {
                player.setCollidable(collidableValue.get(player.getUniqueId()));
            }

            final var game = Main.getInstance().getGameOfPlayer(player);
            ShopUtil.applyTeamUpgrades(player, game);
        }, 2, TaskerTime.TICKS);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedwarsPlayerSpectate(PlayerGameModeChangeEvent e) {
        final var player = e.getPlayer();

        if (!Main.isPlayerInGame(player)) {
            return;
        }
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (e.getNewGameMode() != GameMode.SPECTATOR) {
            return;
        }
        if (!SBAConfig.getInstance().spectator().adventure())
            return;
        if (Main.getInstance().getGameOfPlayer(player).getOriginalOrInheritedKeepInventory()
                && !inventoryContent.containsKey(player.getUniqueId())) {
            inventoryContent.put(player.getUniqueId(),
                    Arrays.asList(player.getInventory().getContents()).stream().map(i -> i != null ? i.clone() : i)
                            .collect(Collectors.toList()).toArray(new ItemStack[0]).clone());
        }
        if(!collidableValue.containsKey(player.getUniqueId()))
            collidableValue.put(player.getUniqueId(), player.isCollidable());
        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {

            player.getInventory().clear();

            if (SBAConfig.getInstance().spectator().teleporter().enabled()) {
                ItemStack compass = SBAConfig.getInstance().spectator().teleporter().get();
                int compassPosition = SBAConfig.getInstance().spectator().teleporter().slot();
                player.getInventory().setItem(compassPosition, compass);
            }

            if (SBAConfig.getInstance().spectator().tracker().enabled()) {
                ItemStack compass = SBAConfig.getInstance().spectator().tracker().get();
                int compassPosition = SBAConfig.getInstance().spectator().tracker().slot();

                var game = Main.getInstance().getGameOfPlayer(player);
                var team = game.getTeamOfPlayer(player);
                if (team != null)
                    player.setCompassTarget(team.getTargetBlock());
                player.getInventory().setItem(compassPosition, compass);
            }

            int leavePosition = SBAConfig.getInstance().spectator().leave().position();
            if (leavePosition >= 0 && leavePosition <= 8) {
                player.getInventory().setItem(leavePosition, SBAConfig.getInstance().spectator().leave().get());
            }
            player.setGameMode(GameMode.ADVENTURE);
        }, 1, TaskerTime.TICKS);
    }

    @EventHandler
    public void onCompassClick(PlayerInteractEvent event) {
        var player = event.getPlayer();
        if ((event.getAction() != Action.RIGHT_CLICK_AIR) && (event.getAction() != Action.RIGHT_CLICK_BLOCK))
            return;
        ItemStack compass = SBAConfig.getInstance().spectator().teleporter().get();
        if (!compass.isSimilar(player.getInventory().getItemInHand()))
            return;

        Logger.info("{} clicked on players teleporter", player);

        PlayerTrackerInventory playerTrackerInventory = new PlayerTrackerInventory(
                Main.getInstance().getGameOfPlayer(player),

                SBAConfig.getInstance().spectator().teleporter().name(),

                (target) -> {
                    player.teleport(target);
                }).openForPlayer(player);

    }

    @EventHandler
    public void onTrackerClick(PlayerInteractEvent event) {
        var player = event.getPlayer();
        if ((event.getAction() != Action.RIGHT_CLICK_AIR) && (event.getAction() != Action.RIGHT_CLICK_BLOCK))
            return;
        ItemStack compass = SBAConfig.getInstance().spectator().tracker().get();
        if (!compass.isSimilar(player.getInventory().getItemInHand()))
            return;

        Logger.info("{} clicked on players tracker", player);

        PlayerTrackerInventory playerTrackerInventory = new PlayerTrackerInventory(
                Main.getInstance().getGameOfPlayer(player),
                SBAConfig.getInstance().spectator().teleporter().name(),
                (target) -> {
                    final var game = Main.getInstance().getGameOfPlayer(player);
                    ArenaManager
                            .getInstance()
                            .get(game.getName())
                            .ifPresent(arena -> ((Arena) arena).track(player, target));
                }).openForPlayer(player);

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent ice) {
        if (ice.getClickedInventory() == null)
            return;
        if (ice.getClickedInventory().getHolder() instanceof PlayerTrackerInventory) {
            PlayerTrackerInventory playerTrackerInventory = (PlayerTrackerInventory) ice.getClickedInventory()
                    .getHolder();
            playerTrackerInventory.onInventoryClick(ice);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedwarsPlayerFakeSpectate(PlayerGameModeChangeEvent e) {
        final var player = e.getPlayer();

        if (!Main.isPlayerInGame(player)) {
            return;
        }
        if (player.getGameMode() == GameMode.ADVENTURE) {
            return;
        }
        if (e.getNewGameMode() != GameMode.ADVENTURE) {
            return;
        }
        if (!SBAConfig.getInstance().spectator().adventure())
            return;
        Tasker.runDelayed(DefaultThreads.GLOBAL_THREAD, () -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 360000, 0));
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setCollidable(false);
            final var game = Main.getInstance().getGameOfPlayer(player);
            final var arena = ArenaManager
                    .getInstance().get(game.getName());
            arena.ifPresent(arena_ -> {
                arena_.addHiddenPlayer(player);
            });
        }, 1, TaskerTime.TICKS);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerKilledEvent(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) {
            return;
        }
        final var attacker = (Player) e.getDamager();

        if (!Main.isPlayerInGame(attacker)) {
            return;
        }

        final var game = (Game) Main.getInstance().getGameOfPlayer(attacker);

        if (!game.isProtectionActive(attacker)) {
            return;
        }

        game.removeProtectedPlayer(attacker);
        if (Main.getConfigurator().config.getBoolean("respawn.show-messages")) {
            MiscUtils.sendActionBarMessage(attacker, i18nonly("respawn_protection_end"));
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerKilledEvent(PlayerDeathEvent e) {
        final var victim = e.getEntity();

        if (!Main.isPlayerInGame(victim)) {
            return;
        }
        final var game = Main.getInstance().getGameOfPlayer(victim);
        // query arena instance for access to Victim/Killer data
        ArenaManager
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> {
                    arena.removeVisualsForPlayer(victim);
                    // player has died, increment death counter
                    arena.getPlayerData(victim.getUniqueId())
                            .ifPresent(victimData -> victimData.setDeaths(victimData.getDeaths() + 1));

                    final var killer = victim.getKiller();
                    Logger.trace("Killer: {}", killer);
                    // killer is present
                    if (killer != null) {
                        // get victim game profile
                        final var gVictim = Main.getPlayerGameProfile(victim);
                        Logger.trace("gVictim: {}", gVictim);

                        if (gVictim == null)
                            return;

                        // get victim team to check if it was a final kill or not
                        final var victimTeam = game.getTeamOfPlayer(victim);
                        Logger.trace("victimTeam: {}", victimTeam);

                        if (victimTeam != null) {
                            arena.getPlayerData(killer.getUniqueId())
                                    .ifPresent(killerData -> {
                                        Logger.trace("Incrementing killer kills to: {}", killerData.getKills() + 1);
                                        // increment kill counter for killer
                                        killerData.setKills(killerData.getKills() + 1);
                                        if (!victimTeam.isAlive()) {
                                            // increment final kill counter for killer
                                            killerData.setFinalKills(killerData.getFinalKills() + 1);
                                            Bukkit.getPluginManager()
                                                    .callEvent(new SBAFinalKillEvent(game, victim, killer));
                                            if (SBAConfig.getInstance().node("final-kill-lightning").getBoolean(true)) {
                                                victim.getWorld().strikeLightningEffect(victim.getLocation());
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

}
