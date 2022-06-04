package io.github.pronze.sba.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.Permissions;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.service.AIService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.citizens.RespawnTrait;
import io.github.pronze.sba.utils.citizens.Strategy;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.SkinTrait;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerDeathMessageSendEvent;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.utils.FakeDeath;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;

import io.github.pronze.sba.config.SBAConfig;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AICommand implements Listener {

    static boolean init = false;

    @OnPostEnable
    public void onPostEnabled() {
        if (init)
            return;
        if (SBAConfig.getInstance().ai().enabled()) {
            CommandManager.getInstance().getAnnotationParser().parse(this);
            SBA.getInstance().registerListener(this);
        }
        init = true;
    }

    @CommandMethod("sba ai leave")
    @CommandPermission("sba.ai")
    @CommandDescription("sba ai leave")
    private void commandAILeave(
            final @NotNull Player player) {
        final var wrapper = SBA.getInstance().getPlayerWrapper(player);

        if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.MESSAGE_NOT_IN_GAME)
                    .send(wrapper);
            return;
        }

        final var game = Main.getInstance().getGameOfPlayer(player);

        Optional<Player> aiPlayer = game.getConnectedPlayers().stream().filter(pl -> AIService.getInstance().isNPC(pl))
                .findFirst();
        aiPlayer.ifPresent(pl -> {
            game.leaveFromGame(pl);
        });
    }

    @CommandMethod("sba ai join")
    @CommandPermission("sba.ai")
    @CommandDescription("sba ai join")
    private void commandAIJoin(
            final @NotNull Player player) {
        final var wrapper = SBA.getInstance().getPlayerWrapper(player);

        if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.MESSAGE_NOT_IN_GAME)
                    .send(wrapper);
            return;
        }
        final var game = Main.getInstance().getGameOfPlayer(player);

        if (game.getStatus() == GameStatus.WAITING) {

            int maxPlayer = game.getMaxPlayers();
            int current = game.countConnectedPlayers();
            if (current < maxPlayer) {
                AIService.getInstance().spawnAI(player.getLocation()).thenAccept(ai -> {
                    Logger.info("{}", ai);
                    int current_ = game.countConnectedPlayers();
                    if (current_ < maxPlayer) {
                        game.joinToGame(ai);
                    } else {
                        AIService.getInstance().getNPC(ai).destroy();
                    }
                });
            }
        }
    }

    @CommandMethod("sba ai join agressive")
    @CommandPermission("sba.ai")
    @CommandDescription("sba ai join agressive")
    private void commandAIJoinAgr(
            final @NotNull Player player) {
        final var wrapper = SBA.getInstance().getPlayerWrapper(player);

        if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.MESSAGE_NOT_IN_GAME)
                    .send(wrapper);
            return;
        }
        final var game = Main.getInstance().getGameOfPlayer(player);

        if (game.getStatus() == GameStatus.WAITING) {

            int maxPlayer = game.getMaxPlayers();
            int current = game.countConnectedPlayers();
            if (current < maxPlayer) {
                AIService.getInstance().spawnAI(player.getLocation(), Strategy.AGRESSIVE)
                        .thenAccept(ai -> {
                            Logger.info("{}", ai);
                            int current_ = game.countConnectedPlayers();
                            if (current_ < maxPlayer) {
                                game.joinToGame(ai);
                            } else {
                                AIService.getInstance().getNPC(ai).destroy();
                            }
                        });
            }
        }
    }

    @CommandMethod("sba ai join defensive")
    @CommandPermission("sba.ai")
    @CommandDescription("sba ai join defensive")
    private void commandAIJoinDef(
            final @NotNull Player player) {
        final var wrapper = SBA.getInstance().getPlayerWrapper(player);

        if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.MESSAGE_NOT_IN_GAME)
                    .send(wrapper);
            return;
        }
        final var game = Main.getInstance().getGameOfPlayer(player);

        if (game.getStatus() == GameStatus.WAITING) {

            int maxPlayer = game.getMaxPlayers();
            int current = game.countConnectedPlayers();
            if (current < maxPlayer) {
                AIService.getInstance().spawnAI(player.getLocation(), Strategy.DEFENSIVE)
                        .thenAccept(ai -> {
                            Logger.info("{}", ai);
                            int current_ = game.countConnectedPlayers();
                            if (current_ < maxPlayer) {
                                game.joinToGame(ai);
                            } else {
                                AIService.getInstance().getNPC(ai).destroy();
                            }
                        });
            }
        }
    }

    @CommandMethod("sba ai join balanced")
    @CommandPermission("sba.ai")
    @CommandDescription("sba ai join balanced")
    private void commandAIJoinBal(
            final @NotNull Player player) {
        final var wrapper = SBA.getInstance().getPlayerWrapper(player);

        if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.MESSAGE_NOT_IN_GAME)
                    .send(wrapper);
            return;
        }
        final var game = Main.getInstance().getGameOfPlayer(player);

        if (game.getStatus() == GameStatus.WAITING) {

            int maxPlayer = game.getMaxPlayers();
            int current = game.countConnectedPlayers();
            if (current < maxPlayer) {
                AIService.getInstance().spawnAI(player.getLocation(), Strategy.BALANCED)
                        .thenAccept(ai -> {
                            Logger.info("{}", ai);
                            int current_ = game.countConnectedPlayers();
                            if (current_ < maxPlayer) {
                                game.joinToGame(ai);
                            } else {
                                AIService.getInstance().getNPC(ai).destroy();
                            }
                        });
            }
        }
    }

}
