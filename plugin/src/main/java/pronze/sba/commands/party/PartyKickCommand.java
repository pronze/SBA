package pronze.sba.commands.party;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.sba.SBA;
import pronze.sba.MessageKeys;
import pronze.sba.events.SBAPlayerPartyKickEvent;
import pronze.sba.wrapper.PlayerWrapper;
import pronze.sba.commands.CommandManager;
import pronze.sba.lib.lang.LanguageService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartyKickCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getManager().getParserRegistry().registerSuggestionProvider("kick", (ctx, s) -> {
            final var player = PlayerMapper
                    .wrapPlayer((Player)ctx.getSender())
                    .as(PlayerWrapper.class);
            final var optionalParty = SBA
                    .getInstance()
                    .getPartyManager()
                    .getPartyOf(player);
            if (optionalParty.isEmpty() || !player.isInParty() || !player.equals(optionalParty.get().getPartyLeader())) {
                return List.of();
            }
            return optionalParty.get()
                    .getMembers()
                    .stream()
                    .map(PlayerWrapper::getName)
                    .filter(name -> !player.getName().equalsIgnoreCase(name))
                    .collect(Collectors.toList());
        });
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party kick <player>")
    private void commandKick(
            final @NotNull Player playerArg,
            final @NotNull @Argument(value = "player", suggestions = "kick") Player toKick
    ) {
        final var player = PlayerMapper
                .wrapPlayer(playerArg)
                .as(PlayerWrapper.class);

        final var args = PlayerMapper
                .wrapPlayer(toKick)
                .as(PlayerWrapper.class);

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

                            if (!party.getMembers().contains(args)) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.PARTY_MESSAGE_PLAYER_NOT_FOUND)
                                        .send(player);
                                return;
                            }

                            final var kickEvent = new SBAPlayerPartyKickEvent(player, party);
                            SBA
                                    .getPluginInstance()
                                    .getServer()
                                    .getPluginManager()
                                    .callEvent(kickEvent);

                            if (kickEvent.isCancelled()) return;



                            party.removePlayer(args);
                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.PARTY_MESSAGE_KICKED)
                                    .replace("%player%", args.getName())
                                    .send(party.getMembers().toArray(PlayerWrapper[]::new));

                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.PARTY_MESSAGE_KICKED_RECEIVED)
                                    .send(args);

                            if (party.getMembers().size() == 1) {
                                SBA
                                        .getInstance()
                                        .getPartyManager()
                                        .disband(party.getUUID());
                            }
                        },() -> LanguageService
                                .getInstance()
                                .get(MessageKeys.PARTY_MESSAGE_ERROR)
                                .send(player)
                );
    }
}
