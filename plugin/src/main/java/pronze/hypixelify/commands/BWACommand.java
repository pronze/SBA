package pronze.hypixelify.commands;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.utils.ShopUtil;

import java.util.List;

import static pronze.hypixelify.lib.lang.I.i18n;

public class BWACommand {

    private final boolean gamesInvEnabled;
    private final BukkitCommandManager<CommandSender> manager;

    public BWACommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        gamesInvEnabled = SBAHypixelify.getConfigurator().config.getBoolean("games-inventory.enabled", true);
    }


    public void build() {
        final var builder = this.manager.commandBuilder("bwa", "bwaddon");

        manager.command(builder.literal("reload")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            Bukkit.getServer().getPluginManager().disablePlugin(SBAHypixelify.getInstance());
                            Bukkit.getServer().getPluginManager().enablePlugin(SBAHypixelify.getInstance());
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
                        .execute(() -> context.getSender().sendMessage(i18n("command_set_lobby_location")))));

        manager.command(builder.literal("reset")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            c.getSender().sendMessage(i18n("command_resetting"));
                            SBAHypixelify.getConfigurator().upgradeCustomFiles();
                        }).execute(() -> context.getSender().sendMessage(i18n("command_reset")))));


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
                                c.getSender().sendMessage(i18n("gamesinv_disabled"));
                                return;
                            }
                            final var player = (Player) c.getSender();
                            final int mode = ShopUtil.getIntFromMode(c.get("gamemode"));
                            if (mode == 0) {
                                player.sendMessage(i18n("command_unknown", true));
                                return;
                            }
                            SBAHypixelify.getGamesInventory().openForPlayer(player, mode);
                        }).execute()));

        manager.command(builder.literal("upgrade")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            if (!SBAHypixelify.isUpgraded()) {
                                c.getSender().sendMessage(i18n("command_cannot_do", true));
                            }
                            SBAHypixelify.getConfigurator().upgradeCustomFiles();
                        }).execute(() -> context.getSender().sendMessage(i18n("command_upgraded", true)))));

        manager.command(builder.literal("cancel")
                .permission("misat11.bw.admin")
                .handler(context -> manager.taskRecipe()
                        .begin(context)
                        .synchronous(c -> {
                            if (!SBAHypixelify.isUpgraded()) {
                                c.getSender().sendMessage(i18n("command_cannot_do", true));
                            }

                            SBAHypixelify.getConfigurator().config.set("version", SBAHypixelify.getInstance().getVersion());
                            SBAHypixelify.getConfigurator().saveConfig();
                        }).execute(() -> context.getSender().sendMessage(i18n("command_cancel_upgrade", true)))));

    }
}

