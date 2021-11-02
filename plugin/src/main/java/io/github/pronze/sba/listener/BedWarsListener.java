package io.github.pronze.sba.listener;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.data.GamePlayerData;
import io.github.pronze.sba.events.SBAFinalKillEvent;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.service.NPCStoreService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.sba.visuals.GameScoreboardManager;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.wrapper.event.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.event.EventPriority;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerDeathEvent;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.npc.skin.NPCSkin;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskerTask;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BedWarsListener implements Listener {
    private final Map<UUID, TaskerTask> runnableCache = new HashMap<>();

    @OnPostEnable
    public void registerListener() {
        SBA.getInstance().registerListener(this);
    }

    @OnPreDisable
    public void onDisable() {
        runnableCache
                .values()
                .forEach(SBAUtil::cancelTask);
        runnableCache.clear();
    }

    @OnEvent
    public void onStarted(BWGameStartedEvent event) {
        final var gameWrapper = event.getGame();

        // safety measure to make sure previous game data was cleared.
        gameWrapper.stop();
        gameWrapper.start();

        // send game start message
        Message.of(LangKeys.GAME_START_MESSAGE)
                .send(gameWrapper
                        .getConnectedPlayers()
                        .stream()
                        .map(PlayerMapper::wrapPlayer)
                        .collect(Collectors.toList()));

        // spawn rotating generators
        if (SBAConfig.getInstance().node("floating-generator", "enabled").getBoolean(true)) {
            for (var itemSpawner : gameWrapper.getItemSpawners()) {
                for (var entry : SBAConfig.getInstance().node("floating-generator", "mapping").childrenMap().entrySet()) {
                    if (itemSpawner.getItemSpawnerType().getMaterial().name().equalsIgnoreCase(((String) entry.getKey()).toUpperCase())) {
                        gameWrapper.createRotatingGenerator(itemSpawner, Material.valueOf(entry.getValue().getString("AIR")));
                    }
                }
            }
        }

        if (SBAConfig.getInstance().node("replace-stores-with-npc").getBoolean(true)) {
            gameWrapper.getGameStoreData().forEach(storeData -> {
                NPCSkin skin;
                List<Component> name;

                final var file = storeData.getShopFile();
                if (file != null && file.equalsIgnoreCase("upgradeShop.yml")) {
                    skin = NPCStoreService.getInstance().getUpgradeShopSkin();
                    name = NPCStoreService.getInstance().getUpgradeShopText();
                } else {
                    skin = NPCStoreService.getInstance().getShopSkin();
                    name = NPCStoreService.getInstance().getShopText();
                }

                final var npc = NPC.of(storeData.getLocation())
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

    @OnEvent
    public void onTargetBlockDestroyed(BWTargetBlockDestroyedEvent event) {
        final var gameWrapper = event.getGame();

        final var destroyedTeam = event.getDestroyedTeam();
        // send bed destroyed message to all players of the team
        final var title = Message.of(LangKeys.BED_DESTROYED_TITLE).asComponent();
        final var subtitle = Message.of(LangKeys.BED_DESTROYED_SUBTITLE).asComponent();

        for (var teamPlayer : destroyedTeam.getConnectedPlayers()) {
            teamPlayer.sendTitle(title, subtitle, 0, 40, 20);
        }

        final var destroyer = event.getDestroyer();
        if (destroyer != null) {
            gameWrapper
                    .getPlayerData(destroyer.getUuid())
                    .ifPresent(GamePlayerData::incrementBedDestroyedCounter);
        }
    }

    @OnEvent
    public void onBedWarsGameEndingEvent(BWGameEndingEvent event) {
        final var gameWrapper = event.getGame();

        final var winningTeam = event.getWinningTeam();
        if (winningTeam != null) {
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

            final var winningTeamPlayerNames = new ArrayList<String>();
            winningTeam.getConnectedPlayers().forEach(player -> winningTeamPlayerNames.add(AdventureHelper.toLegacy(player.getDisplayName())));
            winningTeam.getConnectedPlayers().forEach(pl -> pl.sendTitle(victoryTitle, Component.empty(), 0, 90, 0));


            Message.of(LangKeys.OVERSTATS_MESSAGE)
                    .placeholder("color", winningTeam.getChatColor().toString())
                    .placeholder("win_team", winningTeam.getName())
                    .placeholder("winners", winningTeamPlayerNames.toString())
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
    public void onBWLobbyJoin(BWPlayerJoinedEvent event) {
        final var playerWrapper = event.getPlayer();

        var task = runnableCache.get(playerWrapper.getUuid());
        SBAUtil.cancelTask(task);

        final var gameWrapper = event.getGame();
        switch (gameWrapper.getStatus()) {
            case WAITING:
                task = Tasker.build(taskBase -> {
                    return new Runnable() {
                        int buffer = 1; //fixes the bug where it constantly shows will start in 1 second

                        @Override
                        public void run() {
                            if (gameWrapper.getStatus() == GameStatus.WAITING) {
                                if (gameWrapper.getConnectedPlayers().size() >= gameWrapper.getMinPlayers()) {
                                    String time = gameWrapper.getFormattedTimeLeft();
                                    if (!time.contains("0-1")) {
                                        String[] units = time.split(":");
                                        int seconds = Integer.parseInt(units[1]) + 1;
                                        if (buffer == seconds) return;
                                        buffer = seconds;
                                        if (seconds <= 10) {
                                            var message = Message.of(LangKeys.GAME_STARTS_IN_MESSAGE)
                                                    .placeholder("seconds", String.valueOf(seconds))
                                                    .asComponent();
                                            playerWrapper.sendMessage(message);
                                            playerWrapper.sendTitle(ShopUtil.translateColors("&c" + seconds), "", 0, 20, 0);
                                        }
                                    }
                                }
                            } else {
                                taskBase.cancel();
                                runnableCache.remove(playerWrapper.getUuid());
                            }
                        }
                    };
                }).delay(3L, TaskerTime.TICKS).repeat(1L, TaskerTime.SECONDS).start();
                runnableCache.put(playerWrapper.getUuid(), task);
                break;
            case RUNNING:
                GameScoreboardManager.getInstance().addViewer(playerWrapper);
                gameWrapper.getRotatingGenerators().forEach(generator -> generator.addViewer(playerWrapper));
                break;
        }
    }

    @EventHandler
    public void onBedWarsPlayerLeave(BWPlayerLeaveEvent e) {
        final var player = e.getPlayer();

        final var task = runnableCache.get(player.getUuid());
        SBAUtil.cancelTask(task);
        runnableCache.remove(player.getUuid());

        GameScoreboardManager.getInstance().removeViewer(player);
        player.as(Player.class).setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @OnEvent(priority = EventPriority.LOWEST)
    public void onBedWarsPlayerKilledEvent(SPlayerDeathEvent event) {
        final var victim = event.getPlayer().as(SBAPlayerWrapper.class);
        if (!victim.isInGame()) {
            return;
        }

        final var gameWrapper = victim.getGame();
        gameWrapper
                .getPlayerData(victim.getUniqueId())
                .ifPresent(GamePlayerData::incrementDeathCounter);

        final var killer = event.getKiller();
        if (killer != null) {
            Logger.trace("Killer: {} has killed Player: {}", killer.getName(), victim.getName());

            final var victimTeam = gameWrapper.getTeamOfPlayer(victim);
            if (victimTeam != null) {
                final var maybeData = gameWrapper.getPlayerData(killer.getUuid());
                if (maybeData.isEmpty()) {
                    return;
                }

                final var killerData = maybeData.get();
                killerData.incrementKillCounter();

                Logger.trace("Incremented killer kills to: {}", killerData.getKills());
                if (!victimTeam.isAlive()) {
                    killerData.incrementFinalKillCounter();
                    EventManager.fire(new SBAFinalKillEvent(gameWrapper, victim, killer));
                    if (SBAConfig.getInstance().node("final-kill-lightning").getBoolean(true)) {
                        victim.getLocation().getWorld().as(World.class).strikeLightningEffect(victim.getLocation().as(Location.class));
                    }
                }
            }
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
}
