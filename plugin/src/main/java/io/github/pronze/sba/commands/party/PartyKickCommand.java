package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.wrapper.PlayerSetting;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.events.SBAPlayerPartyKickEvent;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.lib.lang.LanguageService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartyKickCommand {

        static boolean init = false;

        @OnPostEnable
        public void onPostEnable() {
                if (init)
                        return;
                CommandManager.getInstance().getManager().getParserRegistry().registerSuggestionProvider("kick",
                                (ctx, s) -> {
                                        final var player = SBA.getInstance()
                                                        .getPlayerWrapper(((Player) ctx.getSender()));
                                        final var optionalParty = SBA
                                                        .getInstance()
                                                        .getPartyManager()
                                                        .getPartyOf(player);

                                        if (optionalParty.isEmpty()
                                                        || !player.getSettings().isToggled(PlayerSetting.IN_PARTY)
                                                        || !player.equals(optionalParty.get().getPartyLeader())) {
                                                return List.of();
                                        }
                                        return optionalParty.get()
                                                        .getMembers()
                                                        .stream()
                                                        .map(SBAPlayerWrapper::getName)
                                                        .filter(name -> !player.getName().equalsIgnoreCase(name))
                                                        .collect(Collectors.toList());
                                });
                CommandManager.getInstance().getAnnotationParser().parse(this);
                init = true;
        }

        @CommandMethod("party|p kick <player>")
        @CommandPermission("sba.party")
        private void commandKick(
                        final @NotNull Player playerArg,
                        final @NotNull @Argument(value = "player", suggestions = "kick") Player toKick) {
                final var player = SBA.getInstance().getPlayerWrapper((playerArg));

                final var args = SBA.getInstance().getPlayerWrapper((toKick));

                PartyManager
                                .getInstance()
                                .getPartyOf(player)
                                .ifPresentOrElse(party -> {
                                        if (!party.getPartyLeader().equals(player)) {
                                                LanguageService
                                                                .getInstance()
                                                                .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                                                .send(player);
                                                return;
                                        }

                                        if (!party.getMembers().contains(args)) {
                                                LanguageService
                                                                .getInstance()
                                                                .get(MessageKeys.PARTY_MESSAGE_PLAYER_NOT_FOUND)
                                                                .send(player);
                                                return;
                                        }

                                        final var kickEvent = new SBAPlayerPartyKickEvent(player, party);
                                        SBA.getPluginInstance()
                                                        .getServer()
                                                        .getPluginManager()
                                                        .callEvent(kickEvent);

                                        if (kickEvent.isCancelled())
                                                return;

                                        party.removePlayer(args);
                                        LanguageService
                                                        .getInstance()
                                                        .get(MessageKeys.PARTY_MESSAGE_KICKED)
                                                        .replace("%player%", args.as(Player.class).getDisplayName() + ChatColor.RESET)
                                                        .send(party.getMembers().toArray(SBAPlayerWrapper[]::new));

                                        LanguageService
                                                        .getInstance()
                                                        .get(MessageKeys.PARTY_MESSAGE_KICKED_RECEIVED)
                                                        .send(args);

                                        if (party.getMembers().size() == 1) {
                                                PartyManager
                                                                .getInstance()
                                                                .disband(party.getUUID());
                                        }
                                }, () -> LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_ERROR)
                                                .send(player));
        }
}
