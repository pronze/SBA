package io.github.pronze.sba.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.util.SerializableLocation;
import io.github.pronze.sba.visual.npc.NPCManager;
import io.github.pronze.sba.visual.npc.SBANPC;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.npc.skin.NPCSkin;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.annotations.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// modified from bw 0.3.x
@RequiredArgsConstructor
@Service
public final class NPCCommand extends BaseCommand {
    public static final Map<UUID, SBANPC> NPCS_IN_HAND = new ConcurrentHashMap<>();
    public static final List<UUID> SELECTING_NPC = Collections.synchronizedList(new ArrayList<>());
    private final NPCManager npcManager;

    @CommandMethod("sba npc add")
    @CommandPermission("sba.npc.add")
    private void commandAdd(@NotNull PlayerWrapper playerWrapper) {
        var loc = playerWrapper.getLocation();

        var npc = new SBANPC();
        npc.setLocation(new SerializableLocation(loc));
        npc.spawn();
        npcManager.setModified(true);
        npcManager.getNpcs().add(npc);
        NPCS_IN_HAND.put(playerWrapper.getUuid(), npc);
        playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_ADDED)
                .defaultPrefix()
                .placeholder("x", loc.getX(), 2)
                .placeholder("y", loc.getY(), 2)
                .placeholder("z", loc.getZ(), 2)
                .placeholder("yaw", loc.getYaw(), 5)
                .placeholder("pitch", loc.getPitch(), 5));
    }

    @CommandMethod("sba npc select")
    @CommandPermission("sba.npc.select")
    private void commandSelect(@NotNull PlayerWrapper playerWrapper) {
        final var uuid = playerWrapper.getUuid();
        playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NOW_CLICK).defaultPrefix());
        NPCS_IN_HAND.remove(uuid);
        SELECTING_NPC.add(uuid);
    }

    @CommandMethod("sba npc unedit")
    @CommandPermission("sba.npc.unedit")
    private void commandQuit(@NotNull PlayerWrapper playerWrapper) {
        final var uuid = playerWrapper.getUuid();
        if (!NPCS_IN_HAND.containsKey(uuid)) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NOT_EDITING).defaultPrefix());
            return;
        }
        NPCS_IN_HAND.remove(playerWrapper.getUuid());
        playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NO_LONGER_EDITING).defaultPrefix());
    }


    @CommandMethod("sba npc autofocus <focus>")
    @CommandPermission("sba.npc.set_focus")
    private void commandAutoFocus(@NotNull PlayerWrapper playerWrapper,
                                  @Argument("focus") boolean shouldFocus) {
        final var uuid = playerWrapper.getUuid();
        if (!NPCS_IN_HAND.containsKey(uuid)) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NOT_EDITING).defaultPrefix());
            return;
        }

        var npc = NPCS_IN_HAND.get(uuid);
        npc.setShouldLookAtPlayer(shouldFocus);
        npc.getNpc().setShouldLookAtPlayer(shouldFocus);
        npcManager.setModified(true);

        if (shouldFocus) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_SHOULD_LOOK).defaultPrefix());
        } else {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_SHOULD_NOT_LOOK).defaultPrefix());
        }
    }

    @CommandMethod("sba npc remove")
    @CommandPermission("sba.npc.remove")
    private void commandRemove(@NotNull PlayerWrapper playerWrapper) {
        final var uuid = playerWrapper.getUuid();
        if (!NPCS_IN_HAND.containsKey(uuid)) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NOT_EDITING).defaultPrefix());
            return;
        }

        var npc = NPCS_IN_HAND.get(uuid);
        NPCS_IN_HAND.remove(uuid);
        npc.destroy();
        npcManager.setModified(true);
        npcManager.getNpcs().remove(npc);
        playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_REMOVED).defaultPrefix());
    }

    @CommandMethod("sba npc action <action> [value]")
    @CommandPermission("sba.npc.action.set")
    private void commandAction(@NotNull PlayerWrapper playerWrapper,
                               @Argument(value = "action") SBANPC.Action action,
                               @Argument(value = "value", suggestions = "action-value") @Greedy String value) {
        final var uuid = playerWrapper.getUuid();
        if (!NPCS_IN_HAND.containsKey(uuid)) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NOT_EDITING).defaultPrefix());
            return;
        }

        if (value == null || value.isEmpty()) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_REQUIRES_VALUE).defaultPrefix());
            return;
        }

        var npc = NPCS_IN_HAND.get(uuid);
        npc.setAction(action);
        npc.setValue(value);
        npcManager.setModified(true);

        playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_ACTION_SET).defaultPrefix()
                .placeholder("action", action.name())
                .placeholder("value", value));
    }


    @CommandMethod("sba npc skin <value>")
    @CommandPermission("sba.npc.skin.set")
    private void commandSkin(@NotNull PlayerWrapper playerWrapper,
                             @Argument("value") @NotNull String skin) {
        final var uuid = playerWrapper.getUuid();
        if (!NPCS_IN_HAND.containsKey(uuid)) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NOT_EDITING).defaultPrefix());
            return;
        }

        var npc = NPCS_IN_HAND.get(uuid);
        NPCSkin.retrieveSkin(skin).thenAccept(npcSkin -> {
            if (npcSkin == null) {
                playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_SKIN_CHANGE_FAILED).defaultPrefix());
            } else {
                npc.setSkin(npcSkin);
                npc.getNpc().setSkin(npcSkin);
                npcManager.setModified(true);
                playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_SKIN_CHANGED).defaultPrefix());
            }
        });
    }

    @CommandMethod("sba npc hologram addline [line]")
    @CommandPermission("sba.npc.hologram.addline")
    private void commandHologramAddLine(@NotNull PlayerWrapper playerWrapper,
                                        @Argument("line") @Greedy String line) {
        final var uuid = playerWrapper.getUuid();
        if (!NPCS_IN_HAND.containsKey(uuid)) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NOT_EDITING).defaultPrefix());
            return;
        }

        var npc = NPCS_IN_HAND.get(uuid);
        var component = AdventureHelper.toComponent(AdventureHelper.translateAlternateColorCodes('&', line));
        npc.getHologramAbove().add(component);
        npc.getNpc().setDisplayName(npc.getHologramAbove());
        npcManager.setModified(true);
        playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_HOLOGRAM_LINE_ADDED).defaultPrefix()
                .placeholder("line", component));
    }

    @CommandMethod("sba npc hologram setline <position> <line>")
    @CommandPermission("sba.npc.hologram.setline")
    private void commandHologramSetLine(@NotNull PlayerWrapper playerWrapper,
                                        @Argument("position") @Range(from = 0, to = Integer.MAX_VALUE) int pos,
                                        @Argument("line") @Greedy String line) {
        final var uuid = playerWrapper.getUuid();
        if (!NPCS_IN_HAND.containsKey(uuid)) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NOT_EDITING).defaultPrefix());
            return;
        }

        var npc = NPCS_IN_HAND.get(uuid);
        if (npc.getHologramAbove().size() < pos) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_HOLOGRAM_THERES_NO_LINE_NUMBER).defaultPrefix()
                    .placeholder("linenumber", pos));
            return;
        }

        var component = AdventureHelper.toComponent(AdventureHelper.translateAlternateColorCodes('&', line));
        npc.getHologramAbove().set(pos - 1, component);
        npc.getNpc().setDisplayName(npc.getHologramAbove());
        npcManager.setModified(true);
        playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_HOLOGRAM_LINE_SET).defaultPrefix()
                .placeholder("linenumber", pos)
                .placeholder("line", component)
        );
    }

    @CommandMethod("sba npc hologram remove <position>")
    @CommandPermission("sba.npc.hologram.removeline")
    private void commandHologramRemoveLine(@NotNull PlayerWrapper playerWrapper,
                                           @Argument("position") @Range(from = 0, to = Integer.MAX_VALUE) int pos) {
        final var uuid = playerWrapper.getUuid();
        if (!NPCS_IN_HAND.containsKey(uuid)) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NOT_EDITING).defaultPrefix());
            return;
        }

        var npc = NPCS_IN_HAND.get(uuid);
        if (npc.getHologramAbove().size() < pos) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_HOLOGRAM_THERES_NO_LINE_NUMBER).defaultPrefix()
                    .placeholder("linenumber", pos));
            return;
        }
        npc.getHologramAbove().remove(pos - 1);
        npc.getNpc().setDisplayName(npc.getHologramAbove());
        npcManager.setModified(true);
        playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_HOLOGRAM_LINE_REMOVED).defaultPrefix()
                .placeholder("linenumber", pos)
        );
    }

    @CommandMethod("sba npc hologram clear")
    @CommandPermission("sba.npc.hologram.clear")
    private void commandHologramClear(@NotNull PlayerWrapper playerWrapper) {
        final var uuid = playerWrapper.getUuid();
        if (!NPCS_IN_HAND.containsKey(uuid)) {
            playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_NPC_NOT_EDITING).defaultPrefix());
            return;
        }

        var npc = NPCS_IN_HAND.get(uuid);
        npc.getHologramAbove().clear();
        npc.getNpc().setDisplayName(List.of());
        npcManager.setModified(true);
        playerWrapper.sendMessage(Message.of(LangKeys.ADMIN_HOLOGRAM_RESET).defaultPrefix());
    }
}
