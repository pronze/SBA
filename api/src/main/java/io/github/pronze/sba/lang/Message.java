package io.github.pronze.sba.lang;

import io.github.pronze.sba.AddonAPI;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.spectator.Component;

import org.bukkit.ChatColor;
import org.screamingsandals.lib.sender.CommandSenderWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class Message {
    private List<String> original = new ArrayList<>();
    private boolean prefix;


    private static String toMiniMessage(String legacyString)
    {
        String workingString = ChatColor.translateAlternateColorCodes('&', legacyString);

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"0", "<black>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"1", "<dark_blue>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"2", "<dark_green>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"3", "<dark_aqua>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"4", "<dark_red>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"5", "<dark_purple>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"6", "<gold>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"7", "<gray>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"8", "<dark_gray>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"9", "<blue>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"a", "<green>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"A", "<green>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"b", "<aqua>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"B", "<aqua>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"c", "<red>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"C", "<red>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"d", "<light_purple>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"D", "<light_purple>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"e", "<yellow>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"E", "<yellow>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"f", "<white>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"F", "<white>");

        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"k", "<obfuscated>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"K", "<obfuscated>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"l", "<bold>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"L", "<bold>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"m", "<strikethrough>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"M", "<strikethrough>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"n", "<underlined>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"N", "<underlined>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"o", "<italic>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"O", "<italic>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"r", "<reset>");
        workingString = workingString.replaceAll(ChatColor.COLOR_CHAR+"R", "<reset>");

        workingString = workingString.replaceAll("#([0-9a-fA-F]{6})", "<#$1>");

        workingString=ChatColor.stripColor(workingString);
        return workingString;
    }
    public static Message of(List<String> text) {
        return new Message(text);
    }

    private Message(List<String> text) {
        original.addAll(text);
    }

    public Message replace(String key, String value) {
        original = original
                .stream()
                .map(str -> str.replaceAll(key, value))
                .collect(Collectors.toList());
        return this;
    }

    public Message replace(String key, Supplier<String> replacer) {
        original = original
                .stream()
                .map(str -> {
                    return replacer.get();
                })
                .collect(Collectors.toList());
        return this;
    }

    public Message replace(Function<String, String> replacer) {
        var pattern = Pattern.compile("%([a-zA-Z_.,0-9]+)%");
        original = original
                .stream()
                .map(str -> pattern.matcher(str).replaceAll(mr->replacer.apply(mr.group(1))))
                .collect(Collectors.toList());
        return this;
    }

    public Message withPrefix() {
        prefix = true;
        return this;
    }

    public Component toComponent() {
        final var component = Component.text();
        original.forEach(str -> {
            if (prefix) {
                str = AddonAPI
                        .getInstance()
                        .getConfigurator()
                        .getString("prefix", "[SBA]") + ": " + str;
            }
            component.append(Component.fromMiniMessage(toMiniMessage(str)));
            if (original.indexOf(str) + 1 != original.size()) {
                component.append(Component.text("\n"));
            }
        });
        return component.build();
    }

    @Override
    public String toString() {
        var string = original.get(0);
        if (prefix) {
            string = AddonAPI
                    .getInstance()
                    .getConfigurator()
                    .getString("prefix", "[SBA]") + ": ";
        }
        return Component.fromMiniMessage(toMiniMessage(string)).toLegacy();
//        return AdventureHelper.toLegacy(MiniMessage.miniMessage().deserialize());
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
                        str = AddonAPI
                                .getInstance()
                                .getConfigurator()
                                .getString("prefix", "[SBA]") + ": " + str;
                    }
                    return toMiniMessage(str);
                })
                .map(Component::fromMiniMessage)
                .collect(Collectors.toList());
    }

    public void send(CommandSenderWrapper... wrapper) {
        var message = toComponentList();
        for (var sender : wrapper) {
            message.forEach(sender::sendMessage);
        }
    }

    public void send(List<CommandSenderWrapper> wrapperList) {
        var message = toComponentList();
        wrapperList.forEach(wrapper -> message.forEach(wrapper::sendMessage));
    }
}
