package io.github.pronze.sba.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import io.github.pronze.sba.lang.LangKeys;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.command.CloudConstructor;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.sender.CommandSenderWrapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnEnable;
import org.screamingsandals.lib.utils.annotations.methods.Provider;

@RequiredArgsConstructor
@Service(initAnother = {
        SuggestionsProvider.class,
        ReloadCommand.class,
        ShoutCommand.class,
        NPCCommand.class
})
public final class SBACommandManager {
    private CommandManager<CommandSenderWrapper> manager;
    private AnnotationParser<CommandSenderWrapper> annotationParser;

    @SneakyThrows
    @OnEnable
    public void onEnable() {
        manager = CloudConstructor.construct(CommandExecutionCoordinator.simpleCoordinator());

        new MinecraftExceptionHandler<CommandSenderWrapper>()
                .withDefaultHandlers()
                .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION, (senderWrapper, e) ->
                        Message.of(LangKeys.NO_PERMISSIONS).defaultPrefix().getForJoined(senderWrapper)
                )
                .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SYNTAX, (senderWrapper, e) ->
                        Message.of(LangKeys.UNKNOWN_USAGE).defaultPrefix().getForJoined(senderWrapper)
                )
                .apply(manager, s -> s);

        annotationParser = new AnnotationParser<>(
                manager,
                CommandSenderWrapper.class,
                p ->
                        CommandMeta.simple()
                                .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                                .build()
        );
    }

    @NotNull
    @Provider(level = Provider.Level.POST_ENABLE)
    public AnnotationParser<CommandSenderWrapper> provideAnnotationParser() {
        return annotationParser;
    }

    @NotNull
    @Provider(level = Provider.Level.POST_ENABLE)
    public CommandManager<CommandSenderWrapper> provideCommandManager() {
        return manager;
    }
}

