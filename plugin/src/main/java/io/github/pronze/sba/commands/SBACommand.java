package io.github.pronze.sba.commands;


import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.lib.lang.SBALanguageService;
import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Service
public class SBACommand {
    private boolean gamesInvEnabled;

    @OnPostEnable
    public void onPostEnabled() {
        gamesInvEnabled = SBAConfig.getInstance().getBoolean("games-inventory.enabled", true);
        CommandManager.getInstance().getManager().getParserRegistry().registerSuggestionProvider("gameMode", (commandSenderCommandContext, s) -> List.of("solo", "double", "triple", "squad"));
        CommandManager.getInstance().getManager().getParserRegistry().registerSuggestionProvider("maps", (ctx, s) -> Main.getGameNames());
        CommandManager.getInstance().getAnnotationParser().parse(this);
    }

    @CommandMethod("sba reload")
    @CommandDescription("reload command")
    @CommandPermission("sba.reload")
    private void commandReload(
            final @NotNull CommandSender sender
    ) {
        SBAUtil.reloadPlugin(SBA.getPluginInstance());
    }

    @CommandMethod("sba setlobby")
    @CommandDescription("set lobby command")
    @CommandPermission("sba.setlobby")
    private void commandSetLobby(
            final @NotNull Player player
    ) {
        Location location = player.getLocation();
        try {
            final var lobbyNode = SBAConfig.getInstance().node("main-lobby");
            lobbyNode.node("enabled").set(true);
            lobbyNode.node("world").set(location.getWorld().getName());
            lobbyNode.node("x").set(location.getX());
            lobbyNode.node("y").set(location.getY());
            lobbyNode.node("z").set(location.getZ());
            lobbyNode.node("yaw").set(location.getYaw());
            lobbyNode.node("pitch").set(location.getPitch());
            SBAConfig.getInstance().saveConfig();
            Message.of(LangKeys.SUCCESSFULLY_SET_LOBBY).send(PlayerMapper.wrapPlayer(player));
            SBAUtil.reloadPlugin(SBA.getPluginInstance());
        } catch (SerializationException ex) {
            ex.printStackTrace();
        }
    }

    @CommandMethod("sba resetconfig")
    @CommandDescription("reset sba configuration")
    @CommandPermission("sba.reset")
    private void commandReset(
            final @NotNull CommandSender sender
    ) {
        final var wrappedSender = PlayerMapper.wrapSender(sender);
        Message.of(LangKeys.COMMAND_RESETTING).send(wrappedSender);
        SBAConfig.getInstance().upgrade();
        Message.of(LangKeys.RESET_COMMAND_SUCCESS).send(wrappedSender);
    }

    @CommandMethod("sba generate <gamemode> <maps>")
    @CommandDescription("generate games inventory configuration files")
    @CommandPermission("sba.generate")
    private void commandGenerate(
            final @NotNull CommandSender sender,
            final @NotNull @Argument(value = "gamemode", suggestions = "gameMode") String gameMode,
            final @NotNull @Argument(value = "maps", suggestions = "maps") String[] mapsArg
    ) {
        final var stringedGameMode = SBAUtil.capitalizeFirstLetter(gameMode);
        final var maps = List.of(mapsArg);
        if (maps.isEmpty()) {
            //TODO:
            return;
        }
        Logger.trace("Generating Games Inventory file for game mode: {}", gameMode);
        final var file = new File(SBA.getPluginInstance().getDataFolder(), "games-inventory/" + gameMode + ".yml");
        if (file.exists()) {
            Logger.trace("Deleting pre existing games inventory file, status: {}", file.delete());
        }
        try {
            Logger.trace("Creating new games inventory file, status: {}", file.createNewFile());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        var node = SBAConfig.getInstance().node("lobby-scoreboard", "player-size", "games");
        maps.forEach(arg -> {
            try {
                node.node(arg).set(Integer.class, ShopUtil.getIntFromMode(gameMode));
                SBAConfig.getInstance().saveConfig();
                SBAConfig.getInstance().forceReload();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        final var loader = YamlConfigurationLoader.builder()
                .path(file.getAbsoluteFile().toPath())
                .build();
        try {
            var root = loader.load();

            root.node("data").setList(new TypeToken<>() {
            }, List.of(
                    Map.of("stack", "RED_BED;1;§aBed Wars §7(%s);§7Play Bed Wars {§7%s}; ;§eClick to play!".replaceAll(Pattern.quote("%s"), stringedGameMode),
                            "row", "1",
                            "column", "3",
                            "properties", "randomly_join"),

                    Map.of("stack", "BARRIER;1;§cExit",
                            "row", "3",
                            "column", "4",
                            "properties", "exit"),

                    Map.of("stack", "ENDER_PEARL;1;§cClick here to rejoin!;§7Click here to rejoin the lastly joined game.",
                            "properties", "rejoin",
                            "row", "3",
                            "column", "8")
            ));


            root.node("data").appendListNode().set(Map.of("stack", "OAK_SIGN;1;§aMap Selector §7(%s);§7Pick which map you want to play;§7from a list of available servers.; ;§eClick to browse!".replaceAll(Pattern.quote("%s"), stringedGameMode),
                    "row", "1",
                    "column", "5",
                    "options", Map.of("rows", "6",
                            "render_actual_rows", "6"),
                    "items", new ArrayList<Map<String, Object>>() {
                        {
                            final var col = new AtomicInteger(1);
                            final var row = new AtomicInteger(1);
                            maps
                                    .stream()
                                    .map(mapName -> Main.getInstance().getGameByName(mapName))
                                    .forEach(game -> {
                                        col.set(col.get() + 1);
                                        if (col.get() >= 6) {
                                            row.set(row.get() + 1);
                                            col.set(2);
                                        }
                                        if (row.get() >= 3) return;

                                        add(Map.of("stack", ("PAPER;1;§a" + game.getName() + ";§7" + stringedGameMode + "; ;§aClick to play"),
                                                "row", String.valueOf(row.get()),
                                                "column", String.valueOf(col.get()),
                                                "properties", Map.of("name", "join",
                                                        "gameName", game.getName()))
                                        );
                                    });
                        }

                        {
                            add(Map.of("stack", "ARROW;1;§cGo Back",
                                    "row", 4,
                                    "column", 4,
                                    "locate", "main"));
                        }
                    }));

            loader.save(root);

            Message.of(LangKeys.GAMESINV_GENERATED).send(PlayerMapper.wrapSender(sender));
            SBAUtil.reloadPlugin(SBA.getPluginInstance());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @CommandMethod("sba gamesinv <gamemode>")
    @CommandDescription("open GamesInventory for player")
    private void commandGamesInv(
            final @NotNull Player player,
            final @NotNull @Argument(value = "gamemode", suggestions = "gameMode") String gameMode
    ) {
        final var wrappedPlayer = SBAPlayerWrapper.of(player);
        if (!gamesInvEnabled) {
            Message.of(LangKeys.GAMES_INV_DISABLED)
                    .send(wrappedPlayer);
            return;
        }
        final int mode = ShopUtil.getIntFromMode(gameMode);
        GamesInventory
                .getInstance()
                .openForPlayer(wrappedPlayer, mode);
    }

    @CommandMethod("sba upgrade")
    @CommandPermission("sba.upgrade")
    @CommandDescription("upgrade config files")
    private void commandUpgrade(
            final @NotNull CommandSender sender
    ) {
        final var wrappedSender = PlayerMapper.wrapSender(sender);
        if (!SBA.getInstance().isPendingUpgrade()) {
            Message.of(LangKeys.COMMAND_CANNOT_EXECUTE).send(wrappedSender);
            return;
        }
        SBAConfig.getInstance().upgrade();
        Message.of(LangKeys.COMMAND_SUCCESSFULLY_UPGRADED).send(wrappedSender);
    }

    @CommandMethod("sba cancel")
    @CommandDescription("cancel configuration upgrades")
    @CommandPermission("sba.cancel")
    private void commandCancel(
            final @NotNull CommandSender sender
    ) {
        final var wrappedSender = PlayerMapper.wrapSender(sender);
        if (!SBA.getInstance().isPendingUpgrade()) {
            Message.of(LangKeys.CANNOT_DO_COMMAND).send(wrappedSender);
            return;
        }

        try {
            SBAConfig.getInstance().node("version").set(SBA.getInstance().getVersion());
            SBAConfig.getInstance().saveConfig();
            Message.of(LangKeys.COMMAND_CANCEL_UPGRADE).send(wrappedSender);
        } catch (SerializationException ex) {
            ex.printStackTrace();
        }
    }
}

