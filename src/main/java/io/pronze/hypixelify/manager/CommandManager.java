package io.pronze.hypixelify.manager;

import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.commands.AbstractCommand;
import io.pronze.hypixelify.commands.BWACommand;
import io.pronze.hypixelify.commands.PartyCommand;
import io.pronze.hypixelify.commands.ShoutCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final List<AbstractCommand> commands = new ArrayList<>();

    public CommandManager(){
        commands.add(new BWACommand());
        if (SBAHypixelify.getConfigurator().config.getBoolean("party.enabled", true)) {
            commands.add(new PartyCommand());
            commands.add(new ShoutCommand());
        }
    }

    public void registerAll(JavaPlugin plugin){
        if(commands.isEmpty()) return;


        for(AbstractCommand command : commands){
            PluginCommand pluginCommand = plugin.getCommand(command.getCommandName());
            if(pluginCommand == null){
                SBAHypixelify.debug("Command: " + command.getCommandName() + " failed to register");
                continue;
            }
            pluginCommand.setExecutor(command);
        }
    }


}
