package pronze.hypixelify.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyLeaveEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.commands.CommandManager;
import pronze.hypixelify.lib.lang.LanguageService;

@Service
public class PartyLeaveCommand {

    @OnPostEnable
    public void onPostEnable() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("party leave")
    private void commandLeave(
            final @NotNull Player playerArg
    ) {
        final var player = PlayerMapper
                .wrapPlayer(playerArg)
                .as(PlayerWrapper.class);

        if (!player.isInParty()) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_NOT_IN_PARTY)
                    .send(player);
            return;
        }

        SBAHypixelify
                .getInstance()
                .getPartyManager()
                .getPartyOf(player)
                .ifPresentOrElse(party -> {
                    final var event = new SBAPlayerPartyLeaveEvent(player, party);
                    SBAHypixelify
                            .getPluginInstance()
                            .getServer()
                            .getPluginManager()
                            .callEvent(event);
                    if (event.isCancelled()) return;

                    player.setInParty(false);
                    party.removePlayer(player);
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_OFFLINE_QUIT)
                            .replace("%player%", player.getName())
                            .send(party.getMembers().toArray(new PlayerWrapper[0]));

                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_LEFT)
                            .send(player);

                    if (party.getMembers().size() == 1) {
                        SBAHypixelify
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
                                            .replace("%player%", member.getName())
                                            .send(player);
                                }, () -> SBAHypixelify
                                        .getInstance()
                                        .getPartyManager()
                                        .disband(party.getUUID()));
                    }
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.PARTY_MESSAGE_OFFLINE_LEFT)
                            .replace("%player%", player.getName())
                            .send(party.getMembers().stream().filter(member -> !player.equals(member)).toArray(PlayerWrapper[]::new));
                }, () -> LanguageService
                        .getInstance()
                        .get(MessageKeys.PARTY_MESSAGE_ERROR));
    }

}
