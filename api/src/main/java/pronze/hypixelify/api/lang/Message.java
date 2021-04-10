package pronze.hypixelify.api.lang;

import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.minimessage.MiniMessage;
import org.screamingsandals.bedwars.lib.sender.CommandSenderWrapper;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;

import java.util.List;
import java.util.stream.Collectors;

public class Message {
    private List<String> original;

    public static Message of(List<String> text) {
        return new Message(text);
    }

    public Message(List<String> text) {
        original = text;
    }

    public Message replace(String key, String value) {
        original = original
                .stream()
                .map(str-> str.replaceAll(key, value))
                .collect(Collectors.toList());
        return this;
    }

    public Component toComponent() {
        var component = Component.text();
        for (var str : original) {
            component.append(MiniMessage.get().parse(str));
        }
        return component.build();
    }

    @Override
    public String toString() {
        var string = original.get(0);
        return AdventureHelper.toLegacy(MiniMessage.get().parse(string));
    }

    public List<String> toStringList() {
        return original;
    }

    public void send(CommandSenderWrapper... wrapper) {
        var component = toComponent();
        for (var sender : wrapper) {
            sender.sendMessage(component);
        }
    }
}
