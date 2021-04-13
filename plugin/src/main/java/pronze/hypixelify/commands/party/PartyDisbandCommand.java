package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyDisbandEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;

public class PartyDisbandCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyDisbandCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("disband")
                .senderType(Player.class)
                .handler(context -> manager.
                        taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player)ctx.getSender())
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
                                        if (!party.getPartyLeader().equals(player)) {
                                            LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                                    .send(player);
                                            return;
                                        }

                                        final var disbandEvent = new SBAPlayerPartyDisbandEvent(player, party);
                                        SBAHypixelify
                                                .getInstance()
                                                .getServer()
                                                .getPluginManager()
                                                .callEvent(disbandEvent);
                                        if (disbandEvent.isCancelled()) return;

                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_DISBAND)
                                                .send(party.getMembers().toArray(PlayerWrapper[]::new));

                                        SBAHypixelify
                                                .getInstance()
                                                .getPartyManager()
                                                .disband(party.getUUID());
                                    }, () -> LanguageService
                                            .getInstance()
                                            .get(MessageKeys.PARTY_MESSAGE_ERROR)
                                            .send(player));
                        }).execute()));
    }
}
