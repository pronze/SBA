package io.github.pronze.sba.commands.party;

import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.ServiceDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.commands.CommandManager;

@Service
@ServiceDependencies(initAnother = {
                PartyAcceptCommand.class,
                PartyChatCommand.class,
                PartyDebugCommand.class,
                PartyDeclineCommand.class,
                PartyDisbandCommand.class,
                PartyHelpCommand.class,
                PartyListCommand.class,
                PartyInviteCommand.class,
                PartyJoinCommand.class,
                PartyKickCommand.class,
                PartyLeaveCommand.class,
                PartyPromoteCommand.class,
                PartySettingsCommand.class,
                PartyWarpCommand.class
})
public class PartyCommand {
        @OnPostEnable
        public void onPostEnabled() {
                if (SBA.isBroken())
                        return;
                CommandManager.getInstance().getAnnotationParser().parse(this);
        }
}
