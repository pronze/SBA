package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyInviteAcceptEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;

public class PartyAcceptCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyAcceptCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("accept")
                .senderType(Player.class)
                .handler(context -> manager
                        .taskRecipe()
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

                            final var optionalParty = SBAHypixelify
                                    .getInstance()
                                    .getPartyManager()
                                    .getInvitedPartyOf(player);

                            optionalParty.ifPresentOrElse(party -> {
                                final var acceptEvent = new SBAPlayerPartyInviteAcceptEvent(player, party);
                                SBAHypixelify
                                        .getInstance()
                                        .getServer()
                                        .getPluginManager()
                                        .callEvent(acceptEvent);
                                if (acceptEvent.isCancelled()) {
                                    return;
                                }
                                player.setInvitedToAParty(false);
                                player.setInParty(true);
                                party.addPlayer(player);

                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.PARTY_MESSAGE_ACCEPTED)
                                        .replace("%player%", player.getName())
                                        .send(party.getMembers().toArray(PlayerWrapper[]::new));
                            }, () -> LanguageService
                                    .getInstance()
                                    .get(MessageKeys.PARTY_MESSAGE_ERROR)
                                    .send(player));
                        }).execute()));
    }
}
