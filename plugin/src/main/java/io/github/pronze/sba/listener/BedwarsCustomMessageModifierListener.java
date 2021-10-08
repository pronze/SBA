package io.github.pronze.sba.listener;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.lib.lang.SBALanguageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsBedDestroyedMessageSendEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerDeathMessageSendEvent;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

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
            game.getConnectedPlayers().forEach(gPlayer -> Message.of(LangKeys.TEAM_ELIMINATED_MESSAGE)
                    .placeholder("<team>", teamColorStr + playerTeam.getName())
                    .send(PlayerMapper.wrapPlayer(gPlayer)));
        }
    }

    @EventHandler
    public void onBedWarsBedDestroyedMessageSendEvent(BedwarsBedDestroyedMessageSendEvent event) {
        event.setCancelled(true);
        final var teamColorStr = TeamColor.fromApiColor(event.getDestroyedTeam().getColor()).chatColor.toString();
        final var destroyerTeamColorStr = TeamColor.fromApiColor(event.getGame().getTeamOfPlayer(event.getDestroyer()).getColor()).chatColor.toString();

        // TODO: test this.
        final var messages = Message.of(LangKeys.BED_DESTROYED_MESSAGES)
                .placeholder("<team>", teamColorStr + event.getDestroyedTeam().getName())
                .placeholder("<destroyer>", destroyerTeamColorStr + event.getDestroyer().getName())
                .getForAnyone();

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
        final var messages = Message.of(LangKeys.DEATH_MESSAGES_PVP_REGULAR)
                .placeholder("player", victimTeamColorStr + event.getVictim().getName());


        final var killer = victim.getKiller();
        if (killer != null) {
            final var killerTeam = event.getGame().getTeamOfPlayer(killer);
            final var killerTeamColorStr = TeamColor.fromApiColor(killerTeam.getColor()).chatColor.toString();

            messages.placeholder("<killer>", killerTeamColorStr + killer.getName());
            final var list = messages.getForAnyone();
            event.setMessage(AdventureHelper.toLegacy(list.get(RANDOM.nextInt(list.size()))));

            final var lastDamageCause = victim.getLastDamageCause();
            if (lastDamageCause != null) {
                if (lastDamageCause.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    message = Message.of(LangKeys.DEATH_MESSAGES_VOID_KILL)
                            .placeholder("<player>", victimTeamColorStr + victim.getName())
                            .placeholder("<killer>", killerTeamColorStr + killer.getName());
                    event.setMessage(AdventureHelper.toLegacy(message.asComponent()));
                }
            }

        } else {
            message = Message.of(LangKeys.DEATH_MESSAGES_GENERIC)
                    .placeholder("<player>", victimTeamColorStr + victim.getName());
            event.setMessage(AdventureHelper.toLegacy(message.asComponent()));

            final var lastDamageCause = victim.getLastDamageCause();
            if (lastDamageCause != null) {
                if (lastDamageCause.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    message = Message.of(LangKeys.DEATH_MESSAGES_VOID_DEATH)
                            .placeholder("<player>", victimTeamColorStr + victim.getName());
                    event.setMessage(AdventureHelper.toLegacy(message.asComponent()));
                }
            }
        }

        if (!victimTeam.isTargetBlockExists()) {
            final var finalKillPrefix = AdventureHelper.toLegacy(Message.of(LangKeys.FINAL_KILL_PREFIX).asComponent());
            event.setMessage(event.getMessage() + " " + finalKillPrefix);
        }
    }
}
