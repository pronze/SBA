package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.parsers.PlayerArgument;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyKickEvent;
import pronze.hypixelify.game.PlayerWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class PartyKickCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyKickCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("kick")
                .argument(PlayerArgument.<CommandSender>newBuilder("party-participant")
                        .withSuggestionsProvider((ctx, str) -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player)ctx.getSender())
                                    .as(PlayerWrapper.class);
                            final var optionalParty = SBAHypixelify
                                    .getInstance()
                                    .getPartyManager()
                                    .getPartyOf(player);
                            if (optionalParty.isEmpty() || !player.isInParty() || !player.equals(optionalParty.get().getPartyLeader())) {
                                return List.of();
                            }
                            return optionalParty.get()
                                    .getMembers()
                                    .stream()
                                    .map(pronze.hypixelify.api.wrapper.PlayerWrapper::getName)
                                    .filter(name -> !player.getName().equalsIgnoreCase(name))
                                    .collect(Collectors.toList());
                        })
                                .asRequired()
                                .build())
                .senderType(Player.class)
                .handler(context -> manager
                        .taskRecipe()
                        .begin(context)
                        .asynchronous(ctx -> {
                            final var player = PlayerMapper
                                    .wrapPlayer((Player)ctx.getSender())
                                    .as(PlayerWrapper.class);
                            final var args = PlayerMapper
                                    .wrapPlayer((Player) ctx.get("party-participant"))
                                    .as(PlayerWrapper.class);

                            SBAHypixelify
                                    .getInstance()
                                    .getPartyManager()
                                    .getPartyOf(player)
                                    .ifPresentOrElse(party -> {
                                        if (!party.getPartyLeader().equals(player)) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.access-denied")
                                                    .forEach(player::sendMessage);
                                            return;
                                        }

                                        if (!party.getMembers().contains(args)) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.player-not-found")
                                                    .forEach(player::sendMessage);
                                            return;
                                        }

                                        final var kickEvent = new SBAPlayerPartyKickEvent(player, party);
                                        SBAHypixelify
                                                .getInstance()
                                                .getServer()
                                                .getPluginManager()
                                                .callEvent(kickEvent);

                                        if (kickEvent.isCancelled()) return;

                                        SBAHypixelify
                                                .getConfigurator()
                                                .getStringList("party.message.kicked")
                                                .stream().map(str -> str.replace("{player}", args.getName()))
                                                .forEach(str -> party.getMembers().forEach(member -> member.getInstance().sendMessage(str)));
                                        party.removePlayer(args);
                                        if (party.getMembers().size() == 1) {
                                            SBAHypixelify
                                                    .getInstance()
                                                    .getPartyManager()
                                                    .disband(party.getUUID());
                                        }
                                    },() -> SBAHypixelify
                                            .getConfigurator()
                                            .getStringList("party.message.error")
                                            .forEach(player::sendMessage));
                        }).execute()));
    }
}
