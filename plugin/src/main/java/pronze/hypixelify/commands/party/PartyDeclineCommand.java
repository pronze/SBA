package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyInviteDeclineEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;

public class PartyDeclineCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyDeclineCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("decline")
                .senderType(Player.class)
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
                                    .as(PlayerWrapper.class);

                            if (!player.isInvitedToAParty()) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.PARTY_MESSAGE_NOT_INVITED)
                                        .send(player);
                                return;
                            }

                            SBAHypixelify
                                    .getInstance()
                                    .getPartyManager()
                                    .getInvitedPartyOf(player)
                                    .ifPresentOrElse(party -> {
                                        final var partyDeclineEvent = new SBAPlayerPartyInviteDeclineEvent(player, party);
                                        SBAHypixelify
                                                .getInstance()
                                                .getServer()
                                                .getPluginManager()
                                                .callEvent(partyDeclineEvent);
                                        if (partyDeclineEvent.isCancelled()) {
                                            return;
                                        }

                                        party.removeInvitedPlayer(player);
                                        player.setInvitedToAParty(false);

                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_DECLINE_OUTGOING)
                                                .send(player);

                                        var message = LanguageService
                                                        .getInstance()
                                                        .get(MessageKeys.PARTY_MESSAGE_DECLINE_INCOMING)
                                                        .replace("%player%", player.getName());

                                        party.getMembers().forEach(member-> message.send((PlayerWrapper)member));

                                        if (party.getMembers().size() == 1) {
                                            SBAHypixelify
                                                    .getInstance()
                                                    .getPartyManager()
                                                    .disband(party.getUUID());
                                        }
                                     }, () -> SBAHypixelify
                                            .getInstance()
                                            .getConfigurator()
                                            .getStringList("party.message.error")
                                            .forEach(player::sendMessage));
                        }).execute()));
    }
}
