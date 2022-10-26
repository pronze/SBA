package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.wrapper.PlayerSetting;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.events.SBAPlayerPartyLeaveEvent;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;

@Service
public class PartyLeaveCommand {

        static boolean init = false;

        @OnPostEnable
        public void onPostEnabled() {
                if (init)
                        return;
                if (SBAConfig.getInstance().party().enabled())
                        CommandManager.getInstance().getAnnotationParser().parse(this);
                init = true;
        }

        @CommandMethod("party|p leave")
        @CommandPermission("sba.party")
        private void commandLeave(
                        final @NotNull Player playerArg) {
                final var player = SBA.getInstance().getPlayerWrapper((playerArg));

                if (!player.getSettings().isToggled(PlayerSetting.IN_PARTY)) {
                        LanguageService
                                        .getInstance()
                                        .get(MessageKeys.PARTY_MESSAGE_NOT_IN_PARTY)
                                        .send(player);
                        return;
                }

                SBA
                                .getInstance()
                                .getPartyManager()
                                .getPartyOf(player)
                                .ifPresentOrElse(party -> {
                                        final var event = new SBAPlayerPartyLeaveEvent(player, party);
                                        SBA
                                                        .getPluginInstance()
                                                        .getServer()
                                                        .getPluginManager()
                                                        .callEvent(event);
                                        if (event.isCancelled())
                                                return;

                                        player.getSettings().disable(PlayerSetting.IN_PARTY);
                                        party.removePlayer(player);
                                        LanguageService
                                                        .getInstance()
                                                        .get(MessageKeys.PARTY_MESSAGE_OFFLINE_QUIT)
                                                        .replace("%player%",
                                                                        player.as(Player.class).getDisplayName()
                                                                                        + ChatColor.RESET)
                                                        .send(party.getMembers().toArray(new SBAPlayerWrapper[0]));

                                        LanguageService
                                                        .getInstance()
                                                        .get(MessageKeys.PARTY_MESSAGE_LEFT)
                                                        .send(player);

                                        if (party.getMembers().size() == 1) {
                                                SBA
                                                                .getInstance()
                                                                .getPartyManager()
                                                                .disband(party.getUUID());
                                                return;
                                        }
                                        if (party.getPartyLeader().equals(player)) {
                                                party
                                                                .getMembers()
                                                                .stream()
                                                                .findAny()
                                                                .ifPresentOrElse(member -> {
                                                                        party.setPartyLeader(member);
                                                                        LanguageService
                                                                                        .getInstance()
                                                                                        .get(MessageKeys.PARTY_MESSAGE_PROMOTED_LEADER)
                                                                                        .replace("%player%", member.as(
                                                                                                        Player.class)
                                                                                                        .getDisplayName()
                                                                                                        + ChatColor.RESET)
                                                                                        .send(player);
                                                                }, () -> SBA
                                                                                .getInstance()
                                                                                .getPartyManager()
                                                                                .disband(party.getUUID()));
                                        }
                                        LanguageService
                                                        .getInstance()
                                                        .get(MessageKeys.PARTY_MESSAGE_OFFLINE_LEFT)
                                                        .replace("%player%",
                                                                        player.as(Player.class).getDisplayName()
                                                                                        + ChatColor.RESET)
                                                        .send(party.getMembers().stream()
                                                                        .filter(member -> !player.equals(member))
                                                                        .toArray(SBAPlayerWrapper[]::new));
                                }, () -> LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_ERROR));
        }

}
