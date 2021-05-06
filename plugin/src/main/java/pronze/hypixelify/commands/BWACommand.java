package pronze.hypixelify.commands;


import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.screamingsandals.bedwars.game.GameManager;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.lib.bukkit.utils.nms.ClassStorage;
import org.screamingsandals.bedwars.lib.ext.cloud.arguments.standard.DoubleArgument;
import org.screamingsandals.bedwars.lib.ext.cloud.arguments.standard.StringArgument;
import org.screamingsandals.bedwars.lib.ext.cloud.arguments.standard.StringArrayArgument;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.configurate.ConfigurateException;
import org.screamingsandals.bedwars.lib.ext.configurate.hocon.HoconConfigurationLoader;
import org.screamingsandals.bedwars.lib.ext.configurate.serialize.SerializationException;
import org.screamingsandals.bedwars.lib.ext.geantyref.TypeToken;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.Permissions;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.inventories.GamesInventory;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ShopUtil;
import pronze.lib.core.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BWACommand {

    private final boolean gamesInvEnabled;
    private final BukkitCommandManager<CommandSender> manager;

    public BWACommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        gamesInvEnabled = SBAHypixelify.getInstance().getConfigurator().getBoolean("games-inventory.enabled", true);
    }

    public void build() {
        final var builder = this.manager.commandBuilder("bwaddon", "bwa");

        manager.command(builder.literal("reload")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            SBAUtil.reloadPlugin(SBAHypixelify.getInstance());
                        })
                        .execute(() -> LanguageService
                                .getInstance()
                                .get(MessageKeys.RELOADED)
                                .send(PlayerMapper.wrapPlayer((Player) context.getSender())))));

        manager.command(builder.literal("setlobby")
                .permission("misat11.bw.admin")
                .senderType(Player.class)
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            Player player = (Player) c.getSender();
                            Location location = player.getLocation();
                            try {
                                SBAConfig.getInstance().node("main-lobby", "enabled").set(true);
                                SBAConfig.getInstance().node("main-lobby", "world").set(location.getWorld().getName());
                                SBAConfig.getInstance().node("main-lobby", "x").set(location.getX());
                                SBAConfig.getInstance().node("main-lobby", "y").set(location.getY());
                                SBAConfig.getInstance().node("main-lobby", "z").set(location.getZ());
                                SBAConfig.getInstance().node("main-lobby", "yaw").set(location.getYaw());
                                SBAConfig.getInstance().node("main-lobby", "pitch").set(location.getPitch());
                                SBAConfig.getInstance().saveConfig();
                            } catch (SerializationException ex) {
                                SBAHypixelify.getExceptionManager().handleException(ex);
                            }
                        })
                        .execute(() -> {
                            var wrapper = PlayerMapper
                                    .wrapPlayer((Player) context.getSender());

                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.SUCCESSFULLY_SET_LOBBY)
                                    .send(wrapper);
                            SBAUtil.reloadPlugin(SBAHypixelify.getInstance());
                        })));

        manager.command(builder.literal("reset")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            var wrapper = PlayerMapper
                                    .wrapPlayer((Player) c.getSender());
                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.COMMAND_RESETTING)
                                    .send(wrapper);
                            SBAConfig.getInstance().upgrade();
                        }).execute(() -> LanguageService
                                .getInstance()
                                .get(MessageKeys.RESET_COMMAND_SUCCESS)
                                .send(PlayerMapper.wrapPlayer((Player) context.getSender())))));


        manager.command(builder.literal("generate")
                .permission(Permissions.GENERATE_GAMES_INV.getKey())
                .argument(StringArgument.<CommandSender>newBuilder("gamemode")
                        .withSuggestionsProvider((ctx, s) -> List.of("solo", "double", "triple", "squad"))
                        .asRequired()
                        .build())
                .argument(StringArrayArgument.of("maps", (ctx, s) -> GameManager.getInstance().getGameNames()))
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(ctx -> {
                            Logger.trace("Generating Games Inventory file for game mode: {}", (String) ctx.get("gamemode"));
                            final var file = new File(SBAHypixelify.getInstance().getDataFolder(), "games-inventory/" + ctx.get("gamemode") + ".yml");
                            if (file.exists()) {
                                Logger.trace("Deleting pre existing games inventory file, status: {}", file.delete());
                            }
                            try {
                                Logger.trace("Creating new games inventory file, status: {}", file.createNewFile());
                            } catch (IOException ex) {
                                SBAHypixelify.getExceptionManager().handleException(ex);
                            }
                            var mode = (String) ctx.get("gamemode");
                            var arguments = Arrays.asList((String[]) ctx.get("maps"));
                            var node = SBAConfig.getInstance().node("lobby-scoreboard", "player-size", "games");
                            arguments.forEach(arg -> {
                                try {
                                    node.node(arg).set(Integer.class, ShopUtil.getIntFromMode(mode));
                                } catch (Exception ex) {
                                    SBAHypixelify.getExceptionManager().handleException(ex);
                                }
                            });

                            final var loader = HoconConfigurationLoader.builder()
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
                                                arguments
                                                        .stream()
                                                        .map(mapName -> GameManager.getInstance().getGame(mapName).orElseThrow())
                                                        .forEach(game -> {
                                                            col.set(col.get() + 1);
                                                            if (col.get() >= 6) {
                                                                row.set(row.get() + 1);
                                                                col.set(2);
                                                            }
                                                            if (row.get() >= 5) return;

                                                            add(Map.of("stack", ("PAPER;1;§a" + game.getName() + ";§7" + String.valueOf(mode.charAt(0)).toUpperCase() + mode.substring(1) + "; ;§aClick to play"),
                                                                    "row", String.valueOf(row.get()),
                                                                    "column", String.valueOf(col.get()),
                                                                    "gameName", game.getName())
                                                            );
                                                        });
                                            }
                                        }));

                                loader.save(root);
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GAMESINV_GENERATED)
                                        .send(PlayerMapper.wrapPlayer((Player) ctx.getSender()));
                            } catch (IOException ex) {
                                SBAHypixelify.getExceptionManager().handleException(ex);
                            }
                        }).execute()));

        manager.command(builder.literal("gamesinv")
                .senderType(Player.class)
                .argument(StringArgument.<CommandSender>newBuilder("gamemode")
                        .withSuggestionsProvider((ctx, s) -> List.of("solo", "double", "triples", "squads"))
                        .single()
                        .asRequired()
                        .build())
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            if (!gamesInvEnabled) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.GAMES_INV_DISABLED)
                                        .send(PlayerMapper.wrapPlayer((Player) (c.getSender())));
                                return;
                            }
                            final var player = (Player) c.getSender();
                            final int mode = ShopUtil.getIntFromMode(c.get("gamemode"));
                            GamesInventory
                                    .getInstance()
                                    .openForPlayer(player, mode);
                        }).execute()));

        manager.command(builder.literal("upgrade")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            var wrapper = PlayerMapper
                                    .wrapPlayer(c.getSender());

                            if (!SBAHypixelify.getInstance().isPendingUpgrade()) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.COMMAND_CANNOT_EXECUTE)
                                        .send(wrapper);
                                return;
                            }
                            SBAConfig.getInstance().upgrade();
                            LanguageService
                                    .getInstance()
                                    .get(MessageKeys.COMMAND_SUCCESSFULLY_UPGRADED)
                                    .send(wrapper);
                        }).execute()));

        manager.command(builder.literal("cancel")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            var wrapper = PlayerMapper
                                    .wrapPlayer(c.getSender());

                            if (!SBAHypixelify.getInstance().isPendingUpgrade()) {
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.CANNOT_DO_COMMAND)
                                        .send(wrapper);
                                return;
                            }
                            try {
                                SBAConfig.getInstance().node("version").set(SBAHypixelify.getInstance().getVersion());
                                SBAConfig.getInstance().saveConfig();
                                LanguageService
                                        .getInstance()
                                        .get(MessageKeys.COMMAND_CANCEL_UPGRADE)
                                        .send(wrapper);
                            } catch (SerializationException ex) {
                                SBAHypixelify.getExceptionManager().handleException(ex);
                            }
                        }).execute()));

    }
}

