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
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.events.SBAPlayerPartyPromoteEvent;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartyPromoteCommand {

        static boolean init = false;

        @OnPostEnable
        public void onPostEnable() {
                if(SBA.isBroken())return;
                if (init)
                        return;
                CommandManager.getInstance().getManager().getParserRegistry().registerSuggestionProvider("promote",
                                (ctx, s) -> {
                                        final var optionalParty = PartyManager
                                                        .getInstance()
                                                        .getPartyOf(SBA.getInstance()
                                                                        .getPlayerWrapper(((Player) ctx.getSender())));
                                        if (optionalParty.isEmpty()) {
                                                return List.of();
                                        }
                                        return optionalParty.get()
                                                        .getMembers()
                                                        .stream()
                                                        .map(SBAPlayerWrapper::getName)
                                                        .collect(Collectors.toList());
                                });
                if (SBAConfig.getInstance().party().enabled())
                        CommandManager.getInstance().getAnnotationParser().parse(this);
                init = true;
        }

        @CommandMethod("party|p promote <player>")
        @CommandPermission("sba.party")
        private void commandPromote(
                        final @NotNull Player playerArg,
                        final @NotNull @Argument(value = "player", suggestions = "promote") Player toPromote) {
                final var player = SBA.getInstance().getPlayerWrapper((playerArg));

                final var args = SBA.getInstance().getPlayerWrapper((toPromote));

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
                                        if (!party.getPartyLeader().equals(player)) {
                                                LanguageService
                                                                .getInstance()
                                                                .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                                                .send(player);
                                                return;
                                        }

                                        final var partyPromoteEvent = new SBAPlayerPartyPromoteEvent(player, args);
                                        SBA
                                                        .getPluginInstance()
                                                        .getServer()
                                                        .getPluginManager()
                                                        .callEvent(partyPromoteEvent);

                                        if (partyPromoteEvent.isCancelled())
                                                return;

                                        party.setPartyLeader(args);
                                        LanguageService
                                                        .getInstance()
                                                        .get(MessageKeys.PARTY_MESSAGE_PROMOTED_LEADER)
                                                        .replace("%player%",
                                                                        args.as(Player.class).getDisplayName()
                                                                                        + ChatColor.RESET)
                                                        .send(party.getMembers().toArray(new SBAPlayerWrapper[0]));

                                }, () -> LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_ERROR)
                                                .send(player));
        }

}
