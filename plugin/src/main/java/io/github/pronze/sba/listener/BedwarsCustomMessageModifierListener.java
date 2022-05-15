package io.github.pronze.sba.listener;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.lang.Message;
import io.github.pronze.sba.lib.lang.LanguageService;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsBedDestroyedMessageSendEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerDeathMessageSendEvent;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

import java.util.List;
import java.util.Random;

@Service
public class BedwarsCustomMessageModifierListener implements Listener {
    private static final Random RANDOM = new Random();

    @OnPostEnable
    public void onPostEnable() {
        SBA.getInstance().registerListener(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedwarsPlayerDeath(PlayerDeathEvent event) {
        final var player = event.getEntity();
        final var game = Main.getInstance().getGameOfPlayer(player);
        if (game == null) {
            return;
        }
        final var playerTeam = game.getTeamOfPlayer(player);
        if (playerTeam != null && !playerTeam.isTargetBlockExists() &&
                playerTeam.getConnectedPlayers().size() == 1 && playerTeam.getConnectedPlayers().contains(player)) {
            final var teamColorStr = TeamColor.fromApiColor(playerTeam.getColor()).chatColor.toString();
            game.getConnectedPlayers().forEach(gPlayer -> LanguageService
                    .getInstance()
                    .get(MessageKeys.TEAM_ELIMINATED_MESSAGE)
                    .replace("%team%", teamColorStr + playerTeam.getName())
                    .send(PlayerMapper.wrapPlayer(gPlayer)));
        }
    }

    @EventHandler
    public void onBedWarsBedDestroyedMessageSendEvent(BedwarsBedDestroyedMessageSendEvent event) {
        event.setCancelled(true);
        final var teamColorStr = TeamColor.fromApiColor(event.getDestroyedTeam().getColor()).chatColor.toString();
        final var destroyerTeamColorStr = TeamColor.fromApiColor(event.getGame().getTeamOfPlayer(event.getDestroyer()).getColor()).chatColor.toString();

        final var messages = LanguageService
                .getInstance()
                .get(MessageKeys.BED_DESTROYED_MESSAGES)
                .replace("%team%", teamColorStr + event.getDestroyedTeam().getName())
                .replace("%destroyer%", destroyerTeamColorStr + event.getDestroyer().getDisplayName())
                .toComponentList();

        final var randomlyChosen = messages.get(RANDOM.nextInt(messages.size()));
        event.getVictim().sendMessage(" ");
        PlayerMapper.wrapPlayer(event.getVictim()).sendMessage(randomlyChosen);
        event.getVictim().sendMessage(" ");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedWarsPlayerDeathEvent(BedwarsPlayerDeathMessageSendEvent event) {
        final var victim = event.getVictim();
        final var victimTeam = event.getGame().getTeamOfPlayer(victim);
        final var victimTeamColorStr = TeamColor.fromApiColor(victimTeam.getColor()).chatColor.toString();

        Message message;
        final var messages = LanguageService
                .getInstance()
                .get(MessageKeys.DEATH_MESSAGES_PVP_REGULAR)
                .replace("%player%", victimTeamColorStr + event.getVictim().getDisplayName()+ChatColor.RESET);


        final var killer = victim.getKiller();
        if (killer != null) {
            final var killerTeam = event.getGame().getTeamOfPlayer(killer);
            final var killerTeamColorStr = TeamColor.fromApiColor(killerTeam.getColor()).chatColor.toString();

            messages.replace("%killer%", killerTeamColorStr + killer.getDisplayName()+ChatColor.RESET);
            final var list = messages.toStringList();
            event.setMessage(list.get(RANDOM.nextInt(list.size())));


            final var lastDamageCause = victim.getLastDamageCause();
            if (lastDamageCause != null) {
                if (lastDamageCause.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    message = LanguageService
                            .getInstance()
                            .get(MessageKeys.DEATH_MESSAGES_VOID_KILL)
                            .replace("%player%", victimTeamColorStr + victim.getDisplayName()+ChatColor.RESET)
                            .replace("%killer%", killerTeamColorStr + killer.getDisplayName()+ChatColor.RESET);
                    event.setMessage(message.toString());
                }
            }

        } else {
            message = LanguageService
                    .getInstance()
                    .get(MessageKeys.DEATH_MESSAGES_GENERIC)
                    .replace("%player%", victimTeamColorStr + victim.getDisplayName()+ChatColor.RESET);

                    
            event.setMessage(message.toString());

            final var lastDamageCause = victim.getLastDamageCause();
            if (lastDamageCause != null) {
                if (lastDamageCause.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    message = LanguageService
                            .getInstance()
                            .get(MessageKeys.DEATH_MESSAGES_VOID_DEATH)
                            .replace("%player%", victimTeamColorStr + victim.getDisplayName()+ChatColor.RESET);
                    event.setMessage(message.toString());
                }
            }
        }

        if (!victimTeam.isTargetBlockExists()) {
            final var finalKillPrefix = LanguageService.getInstance().get(MessageKeys.FINAL_KILL_PREFIX).toString();
            event.setMessage(event.getMessage() + " " + finalKillPrefix);
        }
    }
}
