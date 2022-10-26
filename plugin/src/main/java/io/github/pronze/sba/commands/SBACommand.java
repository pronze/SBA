package io.github.pronze.sba.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.inventories.PlayerTrackerInventory;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.service.GamesInventoryService;
import io.github.pronze.sba.utils.Logger;
import io.leangen.geantyref.TypeToken;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.screamingsandals.bedwars.api.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.game.GameStore;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.Pair;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.world.LocationMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.sba.utils.Logger.Level;
import io.github.pronze.sba.visuals.LobbyScoreboardManager;
import io.github.pronze.sba.visuals.MainLobbyVisualsManager;

import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SBACommand {
    private boolean gamesInvEnabled;
    static boolean init = false;
    static List<String> allGameModes = List.of("solo", "double", "triples", "squads");

    @OnPostEnable
    public void onPostEnabled() {
        if (init)
            return;
        gamesInvEnabled = SBAConfig.getInstance().getBoolean("games-inventory.enabled", true);
        CommandManager.getInstance().getManager().getParserRegistry().registerSuggestionProvider("gameMode",
                (commandSenderCommandContext, s) -> GamesInventory.getInstance().getGameModeNames());
        CommandManager.getInstance().getManager().getParserRegistry().registerSuggestionProvider("maps",
                (ctx, s) -> Main.getGameNames());
        CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("sba reload")
    @CommandDescription("reload command")
    @CommandPermission("sba.reload")
    private void commandReload(
            final @NotNull CommandSender sender) {
        SBAUtil.reloadPlugin(SBA.getPluginInstance(), sender);
    }

    @CommandMethod("sba dump")
    @CommandDescription("dump command")
    @CommandPermission("sba.dump")
    private void commandDump(
            final @NotNull CommandSender sender) {
        sender.sendMessage("Java version : " + System.getProperty("java.version"));
        String a = Bukkit.getServer().getClass().getPackage().getName();
        String version = a.substring(a.lastIndexOf('.') + 1);
        sender.sendMessage("Server version : " + version);
        sender.sendMessage("Commit id : " + io.github.pronze.sba.VersionInfo.COMMIT);
        sender.sendMessage("Server : " + Bukkit.getServer().getVersion());

        for (var plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            sender.sendMessage(plugin.getName() + " " + plugin.getDescription().getVersion());
        }
    }

    @CommandMethod("sba debug <enabled>")
    @CommandDescription("debug command")
    @CommandPermission("sba.debug")
    private void commandDebug(
            final @NotNull CommandSender sender,
            final @NotNull @Argument(value = "enabled") boolean enabled) {
        if (enabled)
            Logger.setMode(Level.ALL);
        else
            Logger.setMode(Level.WARNING);

    }

    @CommandMethod("sba test compass")
    @CommandDescription("debug compass command")
    @CommandPermission("sba.debug")
    private void commandTestCompass(
            final @NotNull Player player) {
        PlayerTrackerInventory playerTrackerInventory = new PlayerTrackerInventory(null,
        SBAConfig.getInstance().spectator().teleporter().name(),
                (target) -> {
                    player.teleport(target);
                }).openForPlayer(player);
    }

    @CommandMethod("sba test npc")
    @CommandDescription("debug npc command")
    @CommandPermission("sba.debug")
    private void commandTestNPC(
            final @NotNull Player sender) {

        var player = SBA.getInstance().getPlayerWrapper(sender);
        NPC npc = NPC.of(LocationMapper.wrapLocation(sender.getLocation()))
                .addViewer(player)
                .lookAtPlayer(true)
                .displayName(List.of(
                        Component.text("Test NPC, will despawn after 10 seconds").color(TextColor.color(139, 69, 19))))
                .show();
        Tasker.build(() -> {
            npc.destroy();
        }).delay(10, TaskerTime.SECONDS).start();
    }

    @CommandMethod("sba setlobby")
    @CommandDescription("set lobby command")
    @CommandPermission("sba.setlobby")
    private void commandSetLobby(
            final @NotNull Player player) {
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
            LanguageService
                    .getInstance()
                    .get(MessageKeys.SUCCESSFULLY_SET_LOBBY)
                    .send(PlayerMapper.wrapPlayer(player));

            MainLobbyVisualsManager.getInstance().reload();

        } catch (SerializationException ex) {
            ex.printStackTrace();
        }
    }

    @CommandMethod("sba resetconfig")
    @CommandDescription("reset sba configuration")
    @CommandPermission("sba.reset")
    private void commandReset(
            final @NotNull CommandSender sender) {
        final var component = LanguageService
                .getInstance()
                .get(MessageKeys.COMMAND_RESETTING)
                .toComponent();

        PlayerMapper.wrapSender(sender).sendMessage(component);
        SBAConfig.getInstance().upgrade();
        final var c2 = LanguageService
                .getInstance()
                .get(MessageKeys.RESET_COMMAND_SUCCESS)
                .toComponent();

        PlayerMapper.wrapSender(sender).sendMessage(c2);
    }

    @CommandMethod("sba generate [gamemode] [maps]")
    @CommandDescription("generate games inventory configuration files")
    @CommandPermission("sba.generate")
    private void commandGenerate(
            final @NotNull CommandSender sender,
            final @NotNull @Argument(value = "gamemode", suggestions = "gameMode") String gameMode,
            final @NotNull @Argument(value = "maps", suggestions = "maps") String[] mapsArg) {

        if (gameMode == null) {
            allGameModes.forEach(g -> commandGenerate(sender, g, null));
            return;
        }

        final var stringedGameMode = SBAUtil.capitalizeFirstLetter(gameMode);
        List<String> mapsTmp = null;
        if (mapsArg == null || mapsArg.length == 0) {
            final var message = LanguageService
                    .getInstance()
                    .get(MessageKeys.GAMESINV_NO_MAPS)
                    .toComponent();

            mapsTmp = Main.getGameNames().stream().filter(
                    gname -> Main.getGame(gname).getAvailableTeams().stream()
                            .allMatch(t -> t.getMaxPlayers() == ShopUtil.getIntFromMode(gameMode)))
                    .collect(Collectors.toList());

            PlayerMapper.wrapSender(sender).sendMessage(message);
        }
        final var maps = mapsTmp != null ? mapsTmp : List.of(mapsArg);
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
        /*
         * var node = SBAConfig.getInstance().node("lobby-scoreboard", "player-size",
         * "games");
         * maps.forEach(arg -> {
         * try {
         * node.node(arg).set(Integer.class, ShopUtil.getIntFromMode(gameMode));
         * SBAConfig.getInstance().saveConfig();
         * SBAConfig.getInstance().forceReload();
         * SBAConfig.getInstance().postEnable();
         * } catch (Exception ex) {
         * ex.printStackTrace();
         * }
         * });
         */

        final var loader = YamlConfigurationLoader.builder()
                .path(file.getAbsoluteFile().toPath())
                .build();
        try {
            var root = loader.load();

            root.node("data").setList(new TypeToken<>() {
            }, List.of(
                    Map.of("stack",
                            "RED_BED;1;§aBed Wars §7(%s);§7Play Bed Wars {§7%s}; ;§eClick to play!"
                                    .replaceAll(Pattern.quote("%s"), stringedGameMode),
                            "row", "1",
                            "column", "3",
                            "properties", "randomly_join"),

                    Map.of("stack", "BARRIER;1;§cExit",
                            "row", "3",
                            "column", "4",
                            "properties", "exit"),

                    Map.of("stack",
                            "ENDER_PEARL;1;§cClick here to rejoin!;§7Click here to rejoin the lastly joined game.",
                            "properties", "rejoin",
                            "row", "3",
                            "column", "8")));

            root.node("data").appendListNode().set(Map.of("stack",
                    "OAK_SIGN;1;§aMap Selector §7(%s);§7Pick which map you want to play;§7from a list of available servers.; ;§eClick to browse!"
                            .replaceAll(Pattern.quote("%s"), stringedGameMode),
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
                                    .map(mapName -> Pair.of(Main.getInstance().getGameByName(mapName), mapName))
                                    .forEach(game -> {
                                        if (game.getFirst() != null) {
                                            col.set(col.get() + 1);
                                            if (col.get() > 6) {
                                                row.set(row.get() + 1);
                                                col.set(2);
                                            }
                                            if (row.get() > 3)
                                                return;

                                            add(Map.of("stack",
                                                    ("PAPER;1;§a" + game.getFirst().getName() + ";§7" + stringedGameMode
                                                            + "; ;§aClick to play"),
                                                    "row", String.valueOf(row.get()),
                                                    "column", String.valueOf(col.get()),
                                                    "properties", Map.of("name", "join",
                                                            "gameName", game.getFirst().getName())));
                                        } else {
                                            final var message = LanguageService
                                                    .getInstance()
                                                    .get(MessageKeys.GAMESINV_NO_MAPS_WITH_NAME)
                                                    .replace("%name%", game.getSecond())
                                                    .toComponent();
                                            PlayerMapper.wrapSender(sender).sendMessage(message);
                                        }
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

            final var generated = LanguageService
                    .getInstance()
                    .get(MessageKeys.GAMESINV_GENERATED)
                    .replace("%gamemode%", gameMode)
                    .toComponent();
            PlayerMapper.wrapSender(sender).sendMessage(generated);

            GamesInventoryService.getInstance().destroy();
            GamesInventoryService.getInstance().loadGamesInv();
            GamesInventory.getInstance().loadInventory();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @CommandMethod("sba gamesinv <gamemode>")
    @CommandDescription("open GamesInventory for player")
    private void commandGamesInv(
            final @NotNull Player player,
            final @NotNull @Argument(value = "gamemode", suggestions = "gameMode") String gameMode) {
        if (!gamesInvEnabled) {
            final var disabled = LanguageService
                    .getInstance()
                    .get(MessageKeys.GAMES_INV_DISABLED)
                    .toComponent();
            PlayerMapper.wrapPlayer(player).sendMessage(disabled);
            return;
        }
        GamesInventory
                .getInstance()
                .openForPlayer(player, gameMode);
    }

    @CommandMethod("sba join random <gamemode>")
    @CommandDescription("open GamesInventory for player")
    private void commandJoinRandom(
            final @NotNull Player player,
            final @NotNull @Argument(value = "gamemode", suggestions = "gameMode") String gameMode) {
        if (!gamesInvEnabled) {
            final var disabled = LanguageService
                    .getInstance()
                    .get(MessageKeys.GAMES_INV_DISABLED)
                    .toComponent();
            PlayerMapper.wrapPlayer(player).sendMessage(disabled);
            return;
        }
        var couldNotFindGameMessage = LanguageService
                .getInstance()
                .get(MessageKeys.GAMES_INVENTORY_CANNOT_FIND_GAME);
        final var playerWrapper = PlayerMapper.wrapPlayer(player);
        final var games = GamesInventory.getInstance().getGamesWithMode(gameMode);
        if (games == null || games.isEmpty()) {
            couldNotFindGameMessage.send(playerWrapper);
            return;
        }

        Random r = new Random();
        games.sort(Comparator.comparing(c -> ((Game) c).getConnectedPlayers().size())
                .reversed().thenComparing(c -> r.nextInt()));

        games.stream()
                .filter(game -> game.getStatus() == GameStatus.WAITING)
                .findAny()
                .ifPresentOrElse(game -> game.joinToGame(player),
                        () -> couldNotFindGameMessage.send(playerWrapper));
    }

    @CommandMethod("sba store open [shop]")
    @CommandDescription("open Game store for player")
    @CommandPermission("sba.openshop")
    private void sbaGameStoreOpen(
            final @NotNull Player player,
            final @NotNull @Argument("shop") String storeName) {
        final var game = Main.getInstance().getGameOfPlayer(player);
        if (game != null) {
            GameStore store = null;
            for (var storeToCompare : game.getGameStores()) {
                if ((storeName == null)
                        || (storeToCompare.getShopFile() != null && storeToCompare.getShopFile().equals(storeName)))
                    store = storeToCompare;
            }
            if (store != null) {
                BedwarsOpenShopEvent openShopEvent = new BedwarsOpenShopEvent(game,
                        player, store, null);
                new BukkitRunnable() {
                    public void run() {
                        Bukkit.getServer().getPluginManager().callEvent(openShopEvent);
                    }
                }.runTask(SBA.getPluginInstance());
            }
        }
    }

    @CommandMethod("sba upgrade")
    @CommandPermission("sba.upgrade")
    @CommandDescription("upgrade config files")
    private void commandUpgrade(
            final @NotNull CommandSender sender) {
        if (!SBA.getInstance().isPendingUpgrade()) {
            final var cannotExecute = LanguageService
                    .getInstance()
                    .get(MessageKeys.COMMAND_CANNOT_EXECUTE)
                    .toString();

            PlayerMapper.wrapSender(sender).sendMessage(cannotExecute);
            return;
        }

        SBAConfig.getInstance().upgrade();
        LanguageService.getInstance().load(SBA.getPluginInstance());
        final var upgraded = LanguageService
                .getInstance()
                .get(MessageKeys.COMMAND_SUCCESSFULLY_UPGRADED)
                .toString();

        PlayerMapper.wrapSender(sender).sendMessage(upgraded);
    }

    @CommandMethod("sba updateplugin")
    @CommandPermission("sba.updateplugin")
    @CommandDescription("upgrade plugin version")
    private void commandUpdatePlugin(
            final @NotNull CommandSender sender) {

        if (!SBA.getInstance().isPendingUpdate()) {
            final var cannotExecute = LanguageService
                    .getInstance()
                    .get(MessageKeys.COMMAND_CANNOT_EXECUTE)
                    .toString();

            PlayerMapper.wrapSender(sender).sendMessage(cannotExecute);
            return;
        }

        SBA.getInstance().update(sender);

    }

    @CommandMethod("sba cancel")
    @CommandDescription("cancel configuration upgrades")
    @CommandPermission("sba.cancel")
    private void commandCancel(
            final @NotNull CommandSender sender) {
        if (!SBA.getInstance().isPendingUpgrade()) {
            final var m1 = LanguageService
                    .getInstance()
                    .get(MessageKeys.CANNOT_DO_COMMAND)
                    .toString();

            PlayerMapper.wrapSender(sender).sendMessage(m1);
            return;
        }

        try {
            SBAConfig.getInstance().node("version").set(SBA.getInstance().getVersion());
            SBAConfig.getInstance().saveConfig();
            final var m2 = LanguageService
                    .getInstance()
                    .get(MessageKeys.COMMAND_CANCEL_UPGRADE)
                    .toString();

            PlayerMapper.wrapSender(sender).sendMessage(m2);
        } catch (SerializationException ex) {
            ex.printStackTrace();
        }
    }
}
