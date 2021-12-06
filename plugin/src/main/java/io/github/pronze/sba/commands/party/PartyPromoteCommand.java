package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.events.SBAPlayerPartyPromoteEvent;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.lib.lang.LanguageService;

import java.util.List;
import java.util.stream.Collectors;
/*
@Service
public class PartyPromoteCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getManager().getParserRegistry().registerSuggestionProvider("promote", (ctx, s) -> {
            final var optionalParty = PartyManager
                    .getInstance()
                    .getPartyOf(PlayerMapper
                            .wrapPlayer((Player)ctx.getSender())
                            .as(SBAPlayerWrapper.class));
            if (optionalParty.isEmpty()) {
                return List.of();
            }
            return optionalParty.get()
                    .getMembers()
                    .stream()
                    .map(SBAPlayerWrapper::getName)
                    .collect(Collectors.toList());
        });
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party promote <player>")
    private void commandPromote(
            final @NotNull Player playerArg,
            final @NotNull @Argument(value = "player", suggestions = "promote") Player toPromote
    ) {
        final var player = PlayerMapper
                .wrapPlayer(playerArg)
                .as(SBAPlayerWrapper.class);

        final var args = PlayerMapper
                .wrapPlayer(toPromote)
                .as(SBAPlayerWrapper.class);

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

                    if (partyPromoteEvent.isCancelled()) return;

                    party.setPartyLeader(args);
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_PROMOTED_LEADER)
                            .replace("%player%", args.getName())
                            .send(party.getMembers().toArray(new SBAPlayerWrapper[0]));

                }, () -> LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_ERROR)
                        .send(player));
    }

}
*/