package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyLeaveEvent;
import pronze.hypixelify.game.PlayerWrapperImpl;

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
                                    .as(PlayerWrapperImpl.class);

                            if (!player.isInParty()) {
                                SBAHypixelify
                                        .getConfigurator()
                                        .getStringList("party.message.notinparty")
                                        .forEach(player::sendMessage);
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
                                        SBAHypixelify
                                                .getConfigurator()
                                                .getStringList("party.message.offline-quit")
                                                .stream()
                                                .map(str -> str.replace("{player}", player.getName()))
                                                .forEach(str -> party.getMembers().forEach(member -> member.getInstance().sendMessage(str)));
                                        SBAHypixelify
                                                .getConfigurator()
                                                .getStringList("party.message.left")
                                                .forEach(player::sendMessage);

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
                                                        SBAHypixelify
                                                                .getConfigurator()
                                                                .getStringList("party.message.promoted-leader")
                                                                .stream().map(str -> str.replace("{player}", member.getName()))
                                                                .forEach(str -> party.getMembers().forEach(m -> m.getInstance().sendMessage(str)));
                                                    }, () -> SBAHypixelify
                                                            .getInstance()
                                                            .getPartyManager()
                                                            .disband(party.getUUID()));
                                        }
                                    }, () -> SBAHypixelify
                                            .getConfigurator()
                                            .getStringList("party.message.error")
                                            .forEach(player::sendMessage));
                        }).execute()));
    }
}
