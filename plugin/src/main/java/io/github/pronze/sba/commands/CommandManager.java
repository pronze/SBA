package io.github.pronze.sba.commands;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.commands.party.PartyCommand;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.ServiceDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnEnable;

import io.github.pronze.sba.utils.Logger;
import io.github.pronze.sba.utils.SBAUtil;

import java.util.function.Function;

@Service
@ServiceDependencies(initAnother = {
        SBACommand.class,
        GamesInvNPCCommand.class,
        ShoutCommand.class,
        PartyCommand.class,
        AICommand.class
})
@Getter
public class CommandManager {

    public static CommandManager getInstance() {
        return ServiceManager.get(CommandManager.class);
    }

    private PaperCommandManager<CommandSender> manager;
    private AnnotationParser<CommandSender> annotationParser;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private @NotNull BukkitAudiences bukkitAudiences;

    public static void reload() {
        if(SBA.isBroken())return;
        CommandManager ths = getInstance();
        if(ths!=null)
        {
            ths.bukkitAudiences = BukkitAudiences.create(SBA.getPluginInstance());
            ths.minecraftHelp = new MinecraftHelp<>(
                    "/sba help",
                    ths.bukkitAudiences::sender,
                    ths.manager);
        }
    }
    @OnEnable
    public void onEnable(JavaPlugin plugin) {
        if(SBA.isBroken())return;
        if (manager != null)
            return;
        if(SBA.isBroken())return;
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction = CommandExecutionCoordinator
                .simpleCoordinator();
        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try {
            this.manager = new PaperCommandManager<>(
                    plugin,
                    executionCoordinatorFunction,
                    mapperFunction,
                    mapperFunction);

        } catch (final Exception e) {
            Bukkit.getLogger().severe("Failed to initialize the command manager");
            /* Disable the plugin */
            SBAUtil.disablePlugin(plugin);
            return;
        }

        bukkitAudiences = BukkitAudiences.create(plugin);
        minecraftHelp = new MinecraftHelp<>(
                "/sba help",
                bukkitAudiences::sender,
                manager);

        /*if (manager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            try {
                manager.registerBrigadier();
            } catch (Exception e) {
                Logger.error("Could not register Brigadier\r{}", e);
            }
        }*/

        if (manager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p -> CommandMeta.simple()
                .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                .build();

        annotationParser = new AnnotationParser<>(
                manager,
                CommandSender.class,
                commandMetaFunction);
        annotationParser.parse(this);

    }

    @CommandMethod("sba help [query]")
    @CommandDescription("Help menu")
    private void commandHelp(
            final @NonNull CommandSender sender,
            final @Argument("query") @Greedy String query) {
        sender.sendMessage("commandHelp");
        minecraftHelp.queryCommands(query == null ? "" : query, sender);
    }

  
}