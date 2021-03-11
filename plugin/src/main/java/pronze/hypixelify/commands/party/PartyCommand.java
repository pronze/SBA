package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import pronze.hypixelify.SBAHypixelify;

public class PartyCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
    }

    public void build() {
        if (!SBAHypixelify.getConfigurator().config.getBoolean("party.enabled", true)) return;
        new PartyInviteCommand(manager);
        new PartyAcceptCommand(manager);
        new PartyDeclineCommand(manager);
        new PartyDebugCommand(manager);
        new PartyDisbandCommand(manager);
        new PartyLeaveCommand(manager);
        new PartyPromoteCommand(manager);
        new PartyKickCommand(manager);
        new PartyWarpCommand(manager);
    }
}
