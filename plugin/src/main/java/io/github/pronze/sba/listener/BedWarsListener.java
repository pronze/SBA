package io.github.pronze.sba.listener;

import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.events.SBAFinalKillEvent;
import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.game.GameWrapperImpl;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.service.NPCStoreService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.visuals.GameScoreboardManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.*;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.Game;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.npc.skin.NPCSkin;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.game.GameWrapperManagerImpl;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import org.screamingsandals.lib.utils.reflect.Reflect;
import org.screamingsandals.lib.world.LocationMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BedWarsListener implements Listener {
    private static LivingEntity mockEntity = null;

    private final Map<UUID, BukkitTask> runnableCache = new HashMap<>();

    @OnPostEnable
    public void registerListener() {
        SBA.getInstance().registerListener(this);
    }

    @EventHandler
    public void onStarted(BedwarsGameStartedEvent event) {
        final var gameWrapper = GameWrapper.of(event.getGame());

        // send game start message
        Message.of(LangKeys.GAME_START_MESSAGE)
                .send(gameWrapper
                        .getConnectedPlayers()
                        .stream()
                        .map(PlayerMapper::wrapPlayer)
                        .collect(Collectors.toList()));

        // spawn rotating generators
        if (SBAConfig.getInstance().node("floating-generator", "enabled").getBoolean()) {
            for (var itemSpawner : gameWrapper.getItemSpawners()) {
                for (var entry : SBAConfig.getInstance().node("floating-generator", "mapping").childrenMap().entrySet()) {
                    if (itemSpawner.getItemSpawnerType().getMaterial().name().equalsIgnoreCase(((String) entry.getKey()).toUpperCase())) {
                        gameWrapper.createRotatingGenerator(itemSpawner, Material.valueOf(entry.getValue().getString("AIR")));
                    }
                }
            }
        }

        if (SBAConfig.getInstance().node("replace-stores-with-npc").getBoolean(true)) {
            gameWrapper.getGameStores().forEach(store -> {
                final var nonAPIStore = (GameStore) store;
                final var villager = nonAPIStore.kill();
                if (villager != null) {
                    Main.unregisterGameEntity(villager);
                }

                if (mockEntity == null) {
                    // find a better version independent way to mock entities lol
                    mockEntity = (Bat) gameWrapper.getGameWorld().spawnEntity(gameWrapper.getSpectatorSpawn().clone().add(0, 300, 0), EntityType.BAT);
                    mockEntity.setAI(false);
                }

                // set fake entity to avoid bw listener npe
                Reflect.setField(nonAPIStore, "entity", mockEntity);

                NPCSkin skin = null;
                List<Component> name = null;
                final var file = store.getShopFile();
                try {
                    if (file != null && file.equalsIgnoreCase("upgradeShop.yml")) {
                        skin = NPCStoreService.getInstance().getUpgradeShopSkin();
                        name = NPCStoreService.getInstance().getUpgradeShopText();
                    } else {
                        skin = NPCStoreService.getInstance().getShopSkin();
                        name = NPCStoreService.getInstance().getShopText();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                final var npc = NPC.of(LocationMapper.wrapLocation(store.getStoreLocation()))
                        .setDisplayName(name)
                        .setShouldLookAtPlayer(true)
                        .setTouchable(true)
                        .setSkin(skin);

                if (file != null && file.equals("upgradeShop.yml")) {
                    gameWrapper.registerUpgradeStoreNPC(npc);
                } else {
                    gameWrapper.registerStoreNPC(npc);
                }

                gameWrapper.getConnectedPlayers()
                        .stream()
                        .map(PlayerMapper::wrapPlayer)
                        .forEach(npc::addViewer);
                npc.show();
            });
        }
    }

    // fix for listener handles during BedWars reload.
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
    public void onTargetBlockDestroyed(BedwarsTargetBlockDestroyedEvent event) {
        final var gameWrapper = GameWrapper.of(event.getGame());

        final var team = event.getTeam();
        // send bed destroyed message to all players of the team
        final var title = Message.of(LangKeys.BED_DESTROYED_TITLE).asComponent();
        final var subtitle = Message.of(LangKeys.BED_DESTROYED_SUBTITLE).asComponent();

        for (var teamPlayer : team.getConnectedPlayers()) {
            SBAUtil.sendTitle(PlayerMapper.wrapPlayer(teamPlayer), title, subtitle, 0, 40, 20);
        }

        final var destroyer = event.getPlayer();
        if (destroyer != null) {
            gameWrapper.getPlayerData(destroyer.getUniqueId())
                    .ifPresent(destroyerData -> destroyerData.setBedDestroys(destroyerData.getBedDestroys() + 1));
        }
    }

    @EventHandler
    public void onPostRebuildingEvent(BedwarsPostRebuildingEvent e) {
        final var game = e.getGame();
        GameWrapperManagerImpl
                .getInstance()
                .removeArena(game);
    }

    @EventHandler
    public void onOver(BedwarsGameEndingEvent event) {
        final var gameWrapper = GameWrapper.of(event.getGame());

        final var winner = event.getWinningTeam();
        if (winner != null) {
            final var nullStr = AdventureHelper.toLegacy(Message.of(LangKeys.NONE).asComponent());

            String firstKillerName = nullStr;
            int firstKillerScore = 0;

            for (Map.Entry<UUID, GamePlayerData> entry : gameWrapper.getPlayerDataMap().entrySet()) {
                final var playerData = entry.getValue();
                final var kills = playerData.getKills();
                if (kills > 0 && kills > firstKillerScore) {
                    firstKillerScore = kills;
                    firstKillerName = playerData.getName();
                }
            }

            String secondKillerName = nullStr;
            int secondKillerScore = 0;

            for (Map.Entry<UUID, GamePlayerData> entry : gameWrapper.getPlayerDataMap().entrySet()) {
                final var playerData = entry.getValue();
                final var kills = playerData.getKills();
                final var name = playerData.getName();

                if (kills > 0 && kills > secondKillerScore && !name.equalsIgnoreCase(firstKillerName)) {
                    secondKillerName = name;
                    secondKillerScore = kills;
                }
            }

            String thirdKillerName = nullStr;
            int thirdKillerScore = 0;
            for (Map.Entry<UUID, GamePlayerData> entry : gameWrapper.getPlayerDataMap().entrySet()) {
                final var playerData = entry.getValue();
                final var kills = playerData.getKills();
                final var name = playerData.getName();
                if (kills > 0 && kills > thirdKillerScore && !name.equalsIgnoreCase(firstKillerName) &&
                        !name.equalsIgnoreCase(secondKillerName)) {
                    thirdKillerName = name;
                    thirdKillerScore = kills;
                }
            }

            var victoryTitle = Message.of(LangKeys.VICTORY_TITLE).asComponent();

            final var WinTeamPlayers = new ArrayList<String>();
            winner.getConnectedPlayers().forEach(player -> WinTeamPlayers.add(player.getDisplayName()));
            winner.getConnectedPlayers().forEach(pl ->
                    SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), victoryTitle, Component.empty(), 0, 90, 0));


            Message.of(LangKeys.OVERSTATS_MESSAGE)
                    .placeholder("color", org.screamingsandals.bedwars.game.TeamColor.valueOf(winner.getColor().name()).chatColor.toString())
                    .placeholder("win_team", winner.getName())
                    .placeholder("winners", WinTeamPlayers.toString())
                    .placeholder("first_killer_name", firstKillerName)
                    .placeholder("second_killer_name", secondKillerName)
                    .placeholder("third_killer_name", thirdKillerName)
                    .placeholder("first_killer_score", String.valueOf(firstKillerScore))
                    .placeholder("second_killer_score", String.valueOf(secondKillerScore))
                    .placeholder("third_killer_score", String.valueOf(thirdKillerScore))
                    .send(gameWrapper.getConnectedPlayers().stream().map(PlayerMapper::wrapPlayer).collect(Collectors.toList()));
        }
    }

    @EventHandler
    public void onBWLobbyJoin(BedwarsPlayerJoinedEvent e) {
        final var player = e.getPlayer();
        final var task = runnableCache.get(player.getUniqueId());
        final var game = (Game) e.getGame();
        if (task != null) {
            SBAUtil.cancelTask(task);
        }

        switch (game.getStatus()) {
            case WAITING:
                var bukkitTask = new BukkitRunnable() {
                    int buffer = 1; //fixes the bug where it constantly shows will start in 1 second
                    @Override
                    public void run() {
                        if (game.getStatus() == GameStatus.WAITING) {
                            if (game.getConnectedPlayers().size() >= game.getMinPlayers()) {
                                String time = game.getFormattedTimeLeft();

                                if (!time.contains("0-1")) {
                                    String[] units = time.split(":");
                                    int seconds = Integer.parseInt(units[1]) + 1;
                                    if (buffer == seconds) return;
                                    buffer = seconds;
                                    if (seconds <= 10) {
                                        var message = Message.of(LangKeys.GAME_STARTS_IN_MESSAGE)
                                                .placeholder("seconds", String.valueOf(seconds))
                                                .asComponent();
                                        player.sendMessage(message);
                                        SBAUtil.sendTitle(PlayerMapper.wrapPlayer(player), ShopUtil.translateColors("&c" + seconds), "", 0, 20, 0);
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
                final var arena = GameWrapperManagerImpl
                        .getInstance()
                        .get(game.getName())
                        .orElseThrow();
                GameScoreboardManager.getInstance().addViewer(player);
                arena.getRotatingGenerators().forEach(generator -> generator.addViewer(player));
                break;
        }
    }

    @EventHandler
    public void onBedWarsPlayerLeave(BedwarsPlayerLeaveEvent e) {
        final var player = e.getPlayer();
        final var task = runnableCache.get(player.getUniqueId());
        final var game = e.getGame();
        GameScoreboardManager.getInstance().removeViewer(player);
        if (task != null) {
            SBAUtil.cancelTask(task);
        }
        runnableCache.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerKilledEvent(PlayerDeathEvent e) {
        final var victim = e.getEntity();
        if (!Main.isPlayerInGame(victim)) {
            return;
        }
        final var game = Main.getInstance().getGameOfPlayer(victim);
        // query arena instance for access to Victim/Killer data
        GameWrapperManagerImpl
                .getInstance()
                .get(game.getName())
                .ifPresent(arena -> {
                    // player has died, increment death counter
                    arena.getPlayerData(victim.getUniqueId())
                            .ifPresent(victimData -> victimData.setDeaths(victimData.getDeaths() + 1));

                    final var killer = victim.getKiller();
                    //killer is present
                    if (killer != null) {
                        Logger.trace("Killer: {} has killed Player: {}", killer.getName(), victim.getName());
                        // get victim game profile
                        final var gVictim = Main.getPlayerGameProfile(victim);

                        if (gVictim == null || gVictim.isSpectator) return;

                        // get victim team to check if it was a final kill or not
                        final var victimTeam = game.getTeamOfPlayer(victim);
                        if (victimTeam != null) {
                            arena.getPlayerData(killer.getUniqueId())
                                    .ifPresent(killerData -> {
                                        Logger.trace("Incrementing killer kills to: {}", killerData.getKills() + 1);
                                        // increment kill counter for killer
                                        killerData.setKills(killerData.getKills() + 1);
                                        if (!victimTeam.isAlive()) {
                                            // increment final kill counter for killer
                                            killerData.setFinalKills(killerData.getFinalKills() + 1);
                                            Bukkit.getPluginManager().callEvent(new SBAFinalKillEvent(game, victim, killer));
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
