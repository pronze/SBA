package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.parsers.PlayerArgument;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBAPlayerPartyKickEvent;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;

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
                                            LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                                    .send(player);
                                            return;
                                        }

                                        if (!party.getMembers().contains(args)) {
                                            LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.PARTY_MESSAGE_PLAYER_NOT_FOUND)
                                                    .send(player);
                                            return;
                                        }

                                        final var kickEvent = new SBAPlayerPartyKickEvent(player, party);
                                        SBAHypixelify
                                                .getInstance()
                                                .getServer()
                                                .getPluginManager()
                                                .callEvent(kickEvent);

                                        if (kickEvent.isCancelled()) return;



                                        party.removePlayer(args);
                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_KICKED)
                                                .replace("%player%", args.getName())
                                                .send(party.getMembers().toArray(PlayerWrapper[]::new));

                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_KICKED_RECEIVED)
                                                .send(args);

                                        if (party.getMembers().size() == 1) {
                                            SBAHypixelify
                                                    .getInstance()
                                                    .getPartyManager()
                                                    .disband(party.getUUID());
                                        }
                                    },() -> LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.PARTY_MESSAGE_ERROR)
                                                    .send(player)
                                            );
                        }).execute()));
    }
}
