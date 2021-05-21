package pronze.hypixelify.commands;
import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Init;
import org.screamingsandals.lib.utils.annotations.Service;
import pronze.hypixelify.SBAHypixelify;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@Init(services = {
        SBACommand.class,
        ShoutCommand.class
})
@Getter
public class CommandManager {
    public static CommandManager getInstance() {
        return ServiceManager.get(CommandManager.class);
    }

    private PaperCommandManager<CommandSender> manager;
    private AnnotationParser<CommandSender> annotationParser;
    private CommandConfirmationManager<CommandSender> confirmationManager;
    private MinecraftHelp<CommandSender> minecraftHelp;

    public CommandManager() {
        init(SBAHypixelify.getPluginInstance());
    }

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

        BukkitAudiences bukkitAudiences = BukkitAudiences.create(plugin);
        minecraftHelp = new MinecraftHelp<>(
                "/manhunt help",
                bukkitAudiences::sender,
                manager
        );

        if (manager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            manager.registerBrigadier();
        }
        if (manager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
        confirmationManager = new CommandConfirmationManager<>(
                30L,
                TimeUnit.SECONDS,
                context -> context.getCommandContext().getSender().sendMessage(
                        ChatColor.RED + "Confirmation required. Confirm using /manhunt confirm."),
                sender -> sender.sendMessage(
                        ChatColor.RED + "You don't have any pending commands.")
        );
        confirmationManager.registerConfirmationProcessor(manager);
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                CommandMeta.simple()
                        .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                        .build();

        annotationParser = new AnnotationParser<>(
                manager,
                CommandSender.class,
                commandMetaFunction
        );
    }

    @CommandMethod("sba help [query]")
    @CommandDescription("Help menu")
    private void commandHelp(
            final @NonNull CommandSender sender,
            final @Argument("query") @Greedy String query
    ) {
        minecraftHelp.queryCommands(query == null ? "" : query, sender);
    }
}