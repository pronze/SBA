package pronze.hypixelify.commands.party;

import org.bukkit.command.CommandSender;
import org.screamingsandals.bedwars.lib.ext.cloud.bukkit.BukkitCommandManager;
import pronze.hypixelify.SBAHypixelify;

public class PartyCommand {
    private final BukkitCommandManager<CommandSender> manager;

    public PartyCommand(BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
        build();
    }

    public void build() {
        if (!SBAHypixelify.getConfigurator().config.getBoolean("party.enabled", true)) return;
        new PartyInviteCommand(manager);
    }
}
