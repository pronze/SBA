package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.config.SBAConfig;

public class PartyCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
    }

    public void build() {
        if (!SBAConfig.getInstance().node("party","enabled").getBoolean(true)) return;
        new PartyInviteCommand(manager);
        new PartyAcceptCommand(manager);
        new PartyDeclineCommand(manager);
        new PartyDebugCommand(manager);
        new PartyDisbandCommand(manager);
        new PartyLeaveCommand(manager);
        new PartyPromoteCommand(manager);
        new PartyKickCommand(manager);
        new PartyWarpCommand(manager);
        new PartyChatCommand(manager);
        new PartySettingsCommand(manager);
        new PartyHelpCommand(manager);
    }
}
