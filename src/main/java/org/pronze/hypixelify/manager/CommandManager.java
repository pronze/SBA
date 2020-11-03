package org.pronze.hypixelify.manager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.commands.AbstractCommand;
import org.pronze.hypixelify.commands.BWACommand;
import org.pronze.hypixelify.commands.PartyCommand;
import org.pronze.hypixelify.commands.ShoutCommand;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final static List<AbstractCommand> commands = new ArrayList<>();

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
