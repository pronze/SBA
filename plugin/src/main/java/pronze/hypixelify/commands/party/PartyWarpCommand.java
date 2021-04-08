package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.nms.entity.PlayerUtils;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.player.PlayerManager;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.SBAHypixelifyAPI;
import pronze.hypixelify.api.wrapper.PlayerWrapper;
import pronze.hypixelify.lib.lang.LanguageService;

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
                        .synchronous(ctx -> {
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
                                        if (!player.equals(party.getPartyLeader())) {
                                            LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.PARTY_MESSAGE_ACCESS_DENIED)
                                                    .send(player);
                                            return;
                                        }
                                        if (party.getMembers().size() == 1) {
                                            LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.PARTY_MESSAGE_NO_PLAYERS_TO_WARP)
                                                    .send(player);
                                            return;
                                        }

                                        LanguageService
                                                .getInstance()
                                                .get(MessageKeys.PARTY_MESSAGE_WARP)
                                                .send(player);

                                        final var bwPlayerOptional = PlayerManager
                                                .getInstance()
                                                .getPlayer(player.getUuid());

                                        if (bwPlayerOptional.isPresent()) {
                                            final var game = bwPlayerOptional.get().getGame();

                                            party.getMembers()
                                                    .stream().filter(member -> !player.equals(member))
                                                    .forEach(member -> {
                                                        final var memberGame = PlayerManager
                                                                .getInstance()
                                                                .getGameOfPlayer(player.getUuid())
                                                                .orElse(null);

                                                        if (game != memberGame) {
                                                            if (memberGame != null)
                                                                memberGame.leaveFromGame(member.getInstance());
                                                            game.joinToGame(member.getInstance());
                                                            LanguageService
                                                                    .getInstance()
                                                                    .get(MessageKeys.PARTY_MESSAGE_WARP)
                                                                    .send(member);
                                                        }
                                                    });
                                        } else {
                                            final var leaderLocation = player.getInstance().getLocation();
                                            party.getMembers()
                                                    .stream()
                                                    .filter(member -> !member.equals(player))
                                                    .forEach(member -> {
                                                        PlayerManager
                                                                .getInstance()
                                                                .getGameOfPlayer(member.getUuid())
                                                                .ifPresent(game -> game.leaveFromGame(player.getInstance()));
                                                        PlayerUtils.teleportPlayer(member.getInstance(), leaderLocation);
                                                        LanguageService
                                                                .getInstance()
                                                                .get(MessageKeys.PARTY_MESSAGE_WARP)
                                                                .send(PlayerMapper.wrapPlayer(member));
                                                    });
                                        }
                                    }, () -> LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.PARTY_MESSAGE_ERROR)
                                                    .send(player));
                        }).execute()));
    }
}
