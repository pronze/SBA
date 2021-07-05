package io.github.pronze.sba.commands;


import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.inventories.GamesInventory;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.sender.CommandSenderWrapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
            LanguageService
                    .getInstance()
                    .get(MessageKeys.SUCCESSFULLY_SET_LOBBY)
                    .send(PlayerMapper.wrapPlayer(player));

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

   @CommandMethod("sba generate <gamemode> <maps>")
   @CommandDescription("generate games inventory configuration files")
   @CommandPermission("sba.generate")
   private void commandGenerate(
           final @NotNull CommandSender sender,
           final @NotNull @Argument(value = "gamemode", suggestions = "gameMode") String gameMode,
           final @NotNull @Argument(value = "maps", suggestions = "maps") String[] mapsArg
   ) {
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
                   Map.of("stack", "RED_BED;1;§aBed Wars §7(Solo);§7Play Bed Wars {§7Solo}; ;§eClick to play!",
                           "row", "1",
                           "column", "1",
                           "properties", "join_randomly"),

                   Map.of("stack", "BARRIER;1;§cExit",
                           "row", "3",
                           "column", "4",
                           "properties", "exit"),

                   Map.of("stack", "ENDER_PEARL;1;§cClick here to rejoin!;§7Click here to rejoin the lastly joined game.",
                           "properties", "rejoin",
                           "row", "3",
                           "column", "8")
           ));


           //why does it work this way?, no clue
           root.node("data").appendListNode().set(Map.of("stack", "OAK_SIGN;1;§aMap Selector §7(Solo);§7Pick which map you want to play;§7from a list of available servers.; ;§eClick to browse!",
                   "row", "1",
                   "column", "5",
                   "options", Map.of("rows", "6",
                           "render_actual_rows", "6"),
                   "items", new ArrayList<Map<String, String>>() {
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
                                       if (row.get() >= 5) return;

                                       add(Map.of("stack", ("PAPER;1;§a" + game.getName() + ";§7" + String.valueOf(gameMode.charAt(0)).toUpperCase() + gameMode.substring(1) + "; ;§aClick to play"),
                                               "row", String.valueOf(row.get()),
                                               "column", String.valueOf(col.get()),
                                               "gameName", game.getName())
                                       );
                                   });
                       }
                   }));

           loader.save(root);

           final var generated = LanguageService
                   .getInstance()
                   .get(MessageKeys.GAMESINV_GENERATED)
                   .toComponent();
           PlayerMapper.wrapSender(sender).sendMessage(generated);
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
        if (!gamesInvEnabled) {
            final var disabled = LanguageService
                    .getInstance()
                    .get(MessageKeys.GAMES_INV_DISABLED)
                    .toComponent();
            PlayerMapper.wrapPlayer(player).sendMessage(disabled);
            return;
        }
        final int mode = ShopUtil.getIntFromMode(gameMode);
        GamesInventory
                .getInstance()
                .openForPlayer(player, mode);
    }

    @CommandMethod("sba upgrade")
    @CommandPermission("sba.upgrade")
    @CommandDescription("upgrade config files")
    private void commandUpgrade(
            final @NotNull CommandSender sender
    ) {
        if (!SBA.getInstance().isPendingUpgrade()) {
            final var cannotExecute = LanguageService
                    .getInstance()
                    .get(MessageKeys.COMMAND_CANNOT_EXECUTE)
                    .toString();

            PlayerMapper.wrapSender(sender).sendMessage(cannotExecute);
            return;
        }

        SBAConfig.getInstance().upgrade();
        final var upgraded = LanguageService
                .getInstance()
                .get(MessageKeys.COMMAND_SUCCESSFULLY_UPGRADED)
                .toString();

        PlayerMapper.wrapSender(sender).sendMessage(upgraded);
    }

    @CommandMethod("sba cancel")
    @CommandDescription("cancel configuration upgrades")
    @CommandPermission("sba.cancel")
    private void commandCancel(
            final @NotNull CommandSender sender
    ) {
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

