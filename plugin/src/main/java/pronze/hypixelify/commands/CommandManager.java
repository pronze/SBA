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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.commands.party.PartyCommand;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service(initAnother = {
        SBACommand.class,
        ShoutCommand.class,
        PartyCommand.class
})
@Getter
public class CommandManager {

    public static CommandManager getInstance() {
        return ServiceManager.get(CommandManager.class);
    }

    private PaperCommandManager<CommandSender> manager;
    private AnnotationParser<CommandSender> annotationParser;
    private MinecraftHelp<CommandSender> minecraftHelp;

    @OnPostEnable
    public void onPostEnable() {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                CommandExecutionCoordinator.simpleCoordinator();
        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try {
            this.manager = new PaperCommandManager<>(
                    SBAHypixelify.getPluginInstance(),
                    executionCoordinatorFunction,
                    mapperFunction,
                    mapperFunction
            );
        } catch (final Exception e) {
            Bukkit.getLogger().severe("Failed to initialize the command manager");
            /* Disable the plugin */
            Bukkit.getServer().getPluginManager().disablePlugin(SBAHypixelify.getPluginInstance());
            return;
        }

        BukkitAudiences bukkitAudiences = BukkitAudiences.create(SBAHypixelify.getPluginInstance());
        minecraftHelp = new MinecraftHelp<>(
                "/sba help",
                bukkitAudiences::sender,
                manager
        );

        if (manager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            manager.registerBrigadier();
        }
        if (manager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
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