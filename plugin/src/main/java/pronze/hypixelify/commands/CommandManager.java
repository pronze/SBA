package pronze.hypixelify.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bedwars.lib.ext.cloud.CommandTree;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.CloudBukkitCapabilities;
import org.screamingsandals.bedwars.lib.ext.cloud.execution.AsynchronousCommandExecutionCoordinator;
import org.screamingsandals.bedwars.lib.ext.cloud.execution.CommandExecutionCoordinator;
import org.screamingsandals.bedwars.lib.ext.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.screamingsandals.bedwars.lib.ext.cloud.minecraft.extras.MinecraftHelp;
import org.screamingsandals.bedwars.lib.ext.cloud.paper.PaperCommandManager;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.platform.bukkit.BukkitAudiences;

import java.util.function.Function;

public class CommandManager {
    private BukkitCommandManager<CommandSender> manager;
    private BukkitAudiences bukkitAudiences;
    private MinecraftHelp<CommandSender> minecraftHelp;


    public void init(JavaPlugin plugin) {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();

        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try {
            this.manager = new PaperCommandManager<>(
                    plugin,
                    executionCoordinatorFunction,
                    mapperFunction,
                    mapperFunction
            );
        } catch (final Exception e) {
            plugin.getLogger().severe("Failed to initialize the command manager");
            /* Disable the plugin */
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        this.bukkitAudiences = BukkitAudiences.create(plugin);
        this.minecraftHelp = new MinecraftHelp<>(
                "/bwa help",
                this.bukkitAudiences::sender,
                this.manager
        );
        if (manager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            manager.registerBrigadier();
        }
        if (manager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) this.manager).registerAsynchronousCompletions();
        }

        new MinecraftExceptionHandler<CommandSender>()
                .withInvalidSyntaxHandler()
                .withInvalidSenderHandler()
                .withNoPermissionHandler()
                .withArgumentParsingHandler()
                .withCommandExecutionHandler()
                .apply(manager, bukkitAudiences::sender);

        registerCommands();
    }

    private void registerCommands() {
        new BWACommand(manager).build();
        new ShoutCommand(manager).build();
    }
}