package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.nms.entity.PlayerUtils;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.game.PlayerWrapperImpl;

public class PartyWarpCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyWarpCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("warp")
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
                                    .getPartyManager()
                                    .getPartyOf(player)
                                    .ifPresentOrElse(party -> {
                                        if (!player.equals(party.getPartyLeader())) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.access-denied")
                                                    .forEach(player::sendMessage);
                                            return;
                                        }
                                        if (party.getMembers().size() == 1) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.no-players-warp")
                                                    .forEach(player::sendMessage);
                                            return;
                                        }

                                        SBAHypixelify
                                                .getConfigurator()
                                                .getStringList("party.message.warping")
                                                .forEach(player::sendMessage);

                                        if (Main.isPlayerInGame(player.getInstance())) {
                                            final var game = Main.getInstance().getGameOfPlayer(player.getInstance());
                                            party.getMembers().forEach(member -> {
                                                final var memberGame = Main.getInstance().getGameOfPlayer(player.getInstance());
                                                if (!game.equals(memberGame)) {
                                                    memberGame.leaveFromGame(member.getInstance());
                                                    game.joinToGame(member.getInstance());
                                                    SBAHypixelify
                                                            .getConfigurator()
                                                            .getStringList("party.message.warp")
                                                            .forEach(str -> member.getInstance().sendMessage(str));
                                                }
                                            });
                                        } else {
                                            final var leaderLocation = player.getInstance().getLocation();
                                            party.getMembers().stream().map(PlayerWrapper::getInstance).forEach(member -> {
                                                if (Main.getInstance().isPlayerPlayingAnyGame(member)) {
                                                    Main.getInstance().getGameOfPlayer(member).leaveFromGame(member);
                                                }
                                                PlayerUtils.teleportPlayer(member, leaderLocation);
                                                SBAHypixelify
                                                        .getConfigurator()
                                                        .getStringList("party.message.warp")
                                                        .forEach(member::sendMessage);
                                            });
                                        }
                                    }, () -> SBAHypixelify
                                            .getConfigurator()
                                            .getStringList("party.message.error")
                                            .forEach(player::sendMessage));
                        })));
    }
}
