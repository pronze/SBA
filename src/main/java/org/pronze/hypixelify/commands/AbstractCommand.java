package org.pronze.hypixelify.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.message.Messages;

import java.util.List;

public abstract class AbstractCommand implements TabExecutor {

    private final String perm;
    private final boolean console;
    private final String commandName;


    public AbstractCommand(String perm,
                           boolean consoleUse,
                           String commandName) {
        this.perm = perm;
        this.console = consoleUse;
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }


    @Override
    public boolean onCommand(CommandSender sender,
                             Command command, String s, String[] strings) {

        if (onPreExecute(sender, strings)) {
            if (!console && !(sender instanceof Player)) {
                sender.sendMessage(Messages.command_player_only);
                return true;
            }

            if (perm != null && !sender.hasPermission(perm)) {
                sender.sendMessage(Messages.command_no_permissions);
                return true;
            }

            if (strings == null || strings.length < 1 || strings[0].equalsIgnoreCase("help")) {
                displayHelp(sender);
                return true;
            }

            execute(strings, sender);


            onPostExecute();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return tabCompletion(strings, commandSender);
    }


    public String getPerm(){return perm;}

    public abstract boolean onPreExecute(CommandSender sender, String[] args);

    public abstract void onPostExecute();

    public abstract void execute(String[] args, CommandSender sender);

    public abstract void displayHelp(CommandSender sender);

    public abstract List<String> tabCompletion(String[] args, CommandSender sender);

}
