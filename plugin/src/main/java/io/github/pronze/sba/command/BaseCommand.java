package io.github.pronze.sba.command;

import cloud.commandframework.annotations.AnnotationParser;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.sender.CommandSenderWrapper;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.parameters.ProvidedBy;

@RequiredArgsConstructor
public abstract class BaseCommand {

    @OnPostEnable
    public void onPostEnable(@ProvidedBy(SBACommandManager.class) AnnotationParser<CommandSenderWrapper> annotationParser) {
        annotationParser.parse(this);
    }
}
