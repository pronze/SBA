package io.github.pronze.sba.commands.party;

import org.screamingsandals.lib.utils.annotations.Service;

@Service(initAnother = {
        PartyAcceptCommand.class,
        PartyChatCommand.class,
        PartyDebugCommand.class,
        PartyDeclineCommand.class,
        PartyDisbandCommand.class,
        PartyHelpCommand.class,
        PartyInviteCommand.class,
        PartyKickCommand.class,
        PartyLeaveCommand.class,
        PartyPromoteCommand.class,
        PartySettingsCommand.class,
        PartyWarpCommand.class
})
public class PartyCommand {

}
