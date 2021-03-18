package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyInviteDeclineEvent;
import pronze.hypixelify.game.PlayerWrapperImpl;

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
                                    .as(PlayerWrapperImpl.class);

                            if (!player.isInvitedToAParty()) {
                                SBAHypixelify
                                        .getInstance()
                                        .getConfigurator()
                                        .getStringList("party.message.not-invited")
                                        .forEach(player::sendMessage);
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
                                        SBAHypixelify
                                                .getInstance()
                                                .getConfigurator()
                                                .getStringList("party.message.declined-user")
                                                .forEach(player::sendMessage);

                                        SBAHypixelify
                                                .getInstance()
                                                .getConfigurator()
                                                .getStringList("party.message.declined")
                                                .stream()
                                                .map(str -> str.replace("{player}", player.getName()))
                                                .forEach(str -> party.getMembers().forEach(member -> member.getInstance().sendMessage(str)));

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
