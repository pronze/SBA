package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.parsers.PlayerArgument;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.SBAPlayerPartyPromoteEvent;
import pronze.hypixelify.game.PlayerWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class PartyPromoteCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyPromoteCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        final var builder = this.manager.commandBuilder("party", "p");

        manager.command(builder.literal("promote")
                .senderType(Player.class)
                .argument(PlayerArgument.<CommandSender>newBuilder("party-participant")
                        .withSuggestionsProvider((ctx, str) -> {
                            final var optionalParty = SBAHypixelify
                                    .getInstance()
                                    .getPartyManager()
                                    .getPartyOf(PlayerMapper
                                            .wrapPlayer((Player)ctx.getSender())
                                            .as(PlayerWrapper.class));
                            if (optionalParty.isEmpty()) {
                                return List.of();
                            }
                            return optionalParty.get()
                                    .getMembers()
                                    .stream()
                                    .map(pronze.hypixelify.api.wrapper.PlayerWrapper::getName)
                                    .collect(Collectors.toList());
                        })
                        .asRequired()
                        .build())
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
                                        if (!party.getPartyLeader().equals(player)) {
                                            SBAHypixelify
                                                    .getConfigurator()
                                                    .getStringList("party.message.access-denied")
                                                    .forEach(player::sendMessage);
                                            return;
                                        }

                                        final var partyPromoteEvent = new SBAPlayerPartyPromoteEvent(player, args);
                                        SBAHypixelify
                                                .getInstance()
                                                .getServer()
                                                .getPluginManager()
                                                .callEvent(partyPromoteEvent);

                                        if (partyPromoteEvent.isCancelled()) return;

                                        party.setPartyLeader(args);
                                        SBAHypixelify
                                                .getConfigurator()
                                                .getStringList("party.message.promoted-leader")
                                                .stream().map(str -> str.replace("{player}", args.getName()))
                                                .forEach(str -> party.getMembers().forEach(member -> member.getInstance().sendMessage(str)));
                                    }, () -> SBAHypixelify
                                            .getConfigurator()
                                            .getStringList("party.message.error")
                                            .forEach(player::sendMessage));


                        }).execute()));
    }
}
