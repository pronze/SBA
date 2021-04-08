package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.lang.Lang;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyLeaveEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;

public class PartyLeaveCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyLeaveCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("leave")
                .senderType(Player.class)
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player) ctx.getSender())
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
                                                .getInstance()
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
                                    }, () -> LanguageService
                                            .getInstance()
                                            .get(MessageKeys.PARTY_MESSAGE_ERROR));
                        }).execute()));
    }
}
