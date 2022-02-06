package io.github.pronze.sba;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Permission {
    RELOAD("sba.admin.reload"),

    NPC_ADD("npc.add"),
    NPC_SELECT("npc.select"),
    NPC_SET_FOCUS("npc.set_focus"),
    NPC_REMOVE("npc.remove"),
    NPC_UNEDIT("npc.unedit"),
    NPC_SET_ACTION("npc.action.set"),
    NPC_SET_SKIN("npc.skin.set"),
    NPC_ADD_HOLO_LINE("npc.hologram.addline"),
    NPC_HOLO_SET_LINE("npc.hologram.setline"),
    NPC_HOLO_REMOVE_LINE("npc.hologram.removeline"),
    NPC_HOLO_CLEAR("npc.hologram.clear"),

    BYPASS_SHOUT("shout.bypass");

    @Getter
    private final String key;

    Permission(final @NotNull String key) {
        this.key = "sba." + key;
    }

    public boolean hasPermission(@Nullable String key) {
        return this.key.equalsIgnoreCase(key);
    }
}
