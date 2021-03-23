package pronze.hypixelify.commands;


import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.game.GameManager;
import org.screamingsandals.bedwars.lib.ext.cloud.arguments.standard.StringArgument;
import org.screamingsandals.bedwars.lib.ext.cloud.arguments.standard.StringArrayArgument;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.configurate.ConfigurateException;
import org.screamingsandals.bedwars.lib.ext.configurate.hocon.HoconConfigurationLoader;
import org.screamingsandals.bedwars.lib.ext.geantyref.TypeToken;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.Permissions;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ShopUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static pronze.hypixelify.lib.lang.I.i18n;

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
                        .execute(() -> context.getSender().sendMessage(i18n("reloaded")))));

        manager.command(builder.literal("setlobby")
                .permission("misat11.bw.admin")
                .senderType(Player.class)
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            Player player = (Player) c.getSender();
                            Location location = player.getLocation();

                            //TODO: location serializer
                            SBAHypixelify.getConfigurator().config.set("main-lobby.enabled", true);
                            SBAHypixelify.getConfigurator().config.set("main-lobby.world", location.getWorld().getName());
                            SBAHypixelify.getConfigurator().config.set("main-lobby.x", location.getX());
                            SBAHypixelify.getConfigurator().config.set("main-lobby.y", location.getY());
                            SBAHypixelify.getConfigurator().config.set("main-lobby.z", location.getZ());
                            SBAHypixelify.getConfigurator().config.set("main-lobby.yaw", location.getYaw());
                            SBAHypixelify.getConfigurator().config.set("main-lobby.pitch", location.getPitch());
                            SBAHypixelify.getConfigurator().saveConfig();
                        })
                        .execute(() -> {
                            context.getSender().sendMessage(i18n("command_set_lobby_location"));
                            SBAUtil.reloadPlugin(SBAHypixelify.getInstance());
                        })));

        manager.command(builder.literal("reset")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            c.getSender().sendMessage(i18n("command_resetting"));
                            SBAHypixelify.getConfigurator().upgrade();
                        }).execute(() -> context.getSender().sendMessage(i18n("command_reset")))));


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
                                                "column", "8"),

                                        Map.of("stack", "OAK_SIGN;1;§aMap Selector §7(Solo);§7Pick which map you want to play;§7from a list of available servers.; ;§eClick to browse!",
                                                "row" , "1",
                                                "column", "5",
                                                "options", Map.of("rows", "6",
                                                        "render_actual_rows", "6"),
                                                "items", new ArrayList<Map<String, String>>() {
                                                    {
                                                        final var col = new AtomicInteger(1);
                                                        final var row = new AtomicInteger(1);
                                                        var arg = (String) ctx.get("gamemode");
                                                        String[] maps = ctx.get("maps");
                                                        Arrays.stream(maps)
                                                                .map(mapName -> GameManager.getInstance().getGame(mapName).orElseThrow())
                                                                .forEach(game -> {
                                                                    col.set(col.get() + 1);
                                                                    if (col.get() >= 6) {
                                                                        row.set(row.get() + 1);
                                                                        col.set(2);
                                                                    }
                                                                    if (row.get() >= 5) return;

                                                                    add(
                                                                            Map.of("stack", ("PAPER;1;§a" + game.getName() + ";§7" + String.valueOf(arg.charAt(0)).toUpperCase() + arg.substring(1) + "; ;§aClick to play"),
                                                                                        "row", String.valueOf(row.get()),
                                                                                    "column", String.valueOf(col.get()),
                                                                                    "gameName",  game.getName()
                                                                            )
                                                                    );
                                                                });
                                                    }
                                                })


                                ));
                                try {
                                    loader.save(root);
                                } catch (ConfigurateException ex) {
                                    SBAHypixelify.getExceptionManager().handleException(ex);
                                }
                                ctx.getSender().sendMessage("Successfully created!");
                            } catch (IOException ex) {
                                SBAHypixelify.getExceptionManager().handleException(ex);
                            }
                        }).execute())).command(builder.literal("gamesinv")
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
                                c.getSender().sendMessage(i18n("gamesinv_disabled"));
                                return;
                            }
                            final var player = (Player) c.getSender();
                            final int mode = ShopUtil.getIntFromMode(c.get("gamemode"));
                            if (mode == 0) {
                                player.sendMessage(i18n("command_unknown", true));
                                return;
                            }
                            SBAHypixelify.getInstance().getGamesInventory().openForPlayer(player, mode);
                        }).execute()));

        manager.command(builder.literal("upgrade")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            if (!SBAHypixelify.getInstance().isUpgraded()) {
                                c.getSender().sendMessage(i18n("command_cannot_do", true));
                            }
                            SBAHypixelify.getConfigurator().upgrade();
                        }).execute(() -> context.getSender().sendMessage(i18n("command_upgraded", true)))));

        manager.command(builder.literal("cancel")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            if (!SBAHypixelify.getInstance().isUpgraded()) {
                                c.getSender().sendMessage(i18n("command_cannot_do", true));
                            }

                            SBAHypixelify.getConfigurator().config.set("version", SBAHypixelify.getInstance().getVersion());
                            SBAHypixelify.getConfigurator().saveConfig();
                        }).execute(() -> context.getSender().sendMessage(i18n("command_cancel_upgrade", true)))));

    }
}

