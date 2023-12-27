package io.github.pronze.sba.lang;

import io.github.pronze.sba.AddonAPI;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.screamingsandals.lib.spectator.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.sender.CommandSender;
import org.screamingsandals.lib.sender.CommandSender.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPI;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class Message implements Cloneable {
    private List<Component> original = new ArrayList<>();
    private boolean prefix;

    @Override
    public Message clone() {
        Message newMessage = new Message(original);
        newMessage.prefix = prefix;
        return newMessage;
    }

    public static Component toMiniMessage(String legacyString) {
        String workingString = ChatColor.translateAlternateColorCodes('&', legacyString);

        workingString = workingString.replaceAll(
                "[§&]x[§&]([0-9a-z])[§&]([0-9a-z])[§&]([0-9a-z])[§&]([0-9a-z])[§&]([0-9a-z])[§&]([0-9a-z])",
                "<#$1$2$3$4$5$6>");
        workingString = workingString.replaceAll("[§&]#([0-9a-fA-F]{6})", "<#$1>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "0", "<black>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "1", "<dark_blue>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "2", "<dark_green>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "3", "<dark_aqua>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "4", "<dark_red>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "5", "<dark_purple>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "6", "<gold>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "7", "<gray>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "8", "<dark_gray>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "9", "<blue>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "a", "<green>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "A", "<green>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "b", "<aqua>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "B", "<aqua>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "c", "<red>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "C", "<red>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "d", "<light_purple>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "D", "<light_purple>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "e", "<yellow>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "E", "<yellow>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "f", "<white>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "F", "<white>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "k", "<obfuscated>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "K", "<obfuscated>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "l", "<bold>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "L", "<bold>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "m", "<strikethrough>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "M", "<strikethrough>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "n", "<underlined>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "N", "<underlined>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "o", "<italic>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "O", "<italic>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "r", "<reset>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR + "R", "<reset>");

        workingString = ChatColor.stripColor(workingString);

        return Component.fromMiniMessage(workingString);
        // var mini = MiniMessage.miniMessage().deserialize(workingString);
        // workingString =
        // net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
        // .serialize(mini);

        // return workingString;
    }

    public static Message of(List<String> text) {
        return new Message(text.stream().map(Message::toMiniMessage).collect(Collectors.toList()));
    }

    public static Message ofComponent(List<Component> text) {
        return new Message(text);
    }

    private Message(List<Component> text) {
        original.addAll(text);
    }

    public Message replace(String key, String value) {
        original = original
                .stream()
                .map(str -> str.replaceText(key, value))
                .collect(Collectors.toList());
        return this;
    }

    public Message replace(String key, Component value) {
        original = original
                .stream()
                .map(str -> str.replaceText(key, value.toLegacy()))
                .collect(Collectors.toList());
        return this;
    }

    // public Message replace(String key, Supplier<String> replacer) {
    // original = original
    // .stream()
    // .map(str -> {
    // return replacer.get();
    // })
    // .collect(Collectors.toList());
    // return this;
    // }

    public Message replace(Function<String, String> replacer) {
        //var pattern = Pattern.compile("%([a-zA-Z_.,0-9]+)%");
        var pattern = Pattern.compile("[%]([^%]+)[%]");
        original = original
                .stream()
                .map(str ->
                {
                    return str.replaceText(pattern, (mr)->{
                        return replacer.apply(mr.group(1));
                    } );
                })
                .collect(Collectors.toList());
        return this;
    }

    public Message withPrefix() {
        prefix = true;
        return this;
    }

    public @NotNull Message placeholderFor(@NotNull Player player) {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return replace(placeholder->{
                return PlaceholderAPI.setPlaceholders(player, "%"+placeholder+"%");
            });
            // original = original
            //         .stream()
            //         .map(str -> {
            //             return str;
            //             // return PlaceholderAPI.setPlaceholders(player, str);
            //         })
            //         .collect(Collectors.toList());
        }
        return this;
    }

    public @NotNull Message placeholderFor(@NotNull Player player, @NotNull Player relativeTo) {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return replace(placeholder->{
                return PlaceholderAPI.setRelationalPlaceholders(player,relativeTo, "%"+placeholder+"%");
            });
            // original = original
            //         .stream()
            //         .map(str -> {
                       
            //             //return str;
            //             // return PlaceholderAPI.setRelationalPlaceholders(player, relativeTo, str);
            //         })
            //         .collect(Collectors.toList());
        }
        return this;
    }

    public Component toComponent() {
        final var component = Component.text();
        original.forEach(str -> {
            if (prefix) {
                component.append(toMiniMessage(AddonAPI
                        .getInstance()
                        .getConfigurator()
                        .getString("prefix", "[SBA]") + ": "));
            }
            component.append(str);
            if (original.indexOf(str) + 1 != original.size()) {
                component.append(Component.text("\n"));
            }
        });
        return component.build();
    }

    @Override
    public String toString() {
        var string = original.get(0).toLegacy();
        if (prefix) {
            string = AddonAPI
                    .getInstance()
                    .getConfigurator()
                    .getString("prefix", "[SBA]") + ": " + string;
        }
        return toMiniMessage(string).toLegacy();
        // return AdventureHelper.toLegacy(MiniMessage.miniMessage().deserialize());
    }

    public List<String> toStringList() {
        return toComponentList()
                .stream()
                .map(Component::toLegacy)
                .collect(Collectors.toList());
    }

    public List<Component> toComponentList() {
        return original
                .stream()
                .map(str -> {
                    if (prefix) {
                        str = Component.text().append(toMiniMessage(AddonAPI
                                .getInstance()
                                .getConfigurator()
                                .getString("prefix", "[SBA]") + ": ")).append(str).build();
                    }
                    return str;
                })
                .collect(Collectors.toList());
    }

    public void send(CommandSender... wrapper) {
        // var message = toComponentList();
        // for (var sender : wrapper) {
        // message.forEach(sender::sendMessage);
        // }
        for (var receiver : wrapper) {
            Player target = null;
            if (receiver.getType() == Type.PLAYER) {
                target = receiver.as(Player.class);
            }
            clone().placeholderFor(target).toComponentList().forEach(receiver::sendMessage);
            ;
        }
    }

    public void send(List<CommandSender> wrapperList) {

        wrapperList.forEach(receiver -> {
            Player target = null;
            if (receiver.getType() == Type.PLAYER) {
                target = receiver.as(Player.class);
            }
            clone().placeholderFor(target).toComponentList().forEach(receiver::sendMessage);
            ;
        });
        // var message = toComponentList();
        // wrapperList.forEach(wrapper -> message.forEach(wrapper::sendMessage));
    }

}
