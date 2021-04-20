package pronze.hypixelify.registry;
import pronze.hypixelify.game.StoreWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCRegistry<T> {
    private final Map<T, List<StoreWrapper>> registry = new HashMap<>();

    public boolean isIdentifierPresent(T identifier) {
        return registry.containsKey(identifier);
    }

    public List<StoreWrapper> get(T identifier) {
        var data = registry.get(identifier);
        if (data == null) {
            return List.of();
        }
        return data;
    }

    public void register(T identifier, List<StoreWrapper> npcs) {
        if (isIdentifierPresent(identifier)) {
            throw new UnsupportedOperationException("Identifier: " + identifier.toString() + " is already present in registry!");
        }
        registry.put(identifier, npcs);
    }

    public void register(T identifier, StoreWrapper npc) {
        if (isIdentifierPresent(identifier)) {
            var data = get(identifier);
            if (data != null) {
                data.add(npc);
                return;
            }
        }
        register(identifier, List.of(npc));
    }

    public void unregister(T identifier) {
        registry.remove(identifier);
    }
}
