package io.github.pronze.sba.visual.npc;

import io.github.pronze.sba.command.NPCCommand;
import io.github.pronze.sba.game.GamePlayer;
import io.github.pronze.sba.lang.LangKeys;
import lombok.*;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerJoinEvent;
import org.screamingsandals.lib.event.player.SPlayerLeaveEvent;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.npc.event.NPCInteractEvent;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.InteractType;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.utils.annotations.parameters.ConfigFile;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

// modified from bw 0.3.x
@RequiredArgsConstructor
@Service
@Data
public final class NPCManager {
    @Setter(AccessLevel.NONE)
    @ConfigFile(value = "database/npcdb.json")
    private final GsonConfigurationLoader loader;
    private final List<SBANPC> npcs = new ArrayList<>();
    private boolean modified;

    @OnPostEnable
    public void onPostEnable() {
        try {
            var node = loader.load();

            node.childrenList().forEach(npcNode -> {
                try {
                    var npc = npcNode.get(SBANPC.class);
                    if (npc == null) {
                        return;
                    }
                    npc.spawn();
                    npcs.add(npc);
                } catch (ConfigurateException ex) {
                    ex.printStackTrace();
                }
            });
        } catch (ConfigurateException ex) {
            ex.printStackTrace();
        }
    }

    @OnPreDisable
    public void onPreDisable() {
        if (npcs.isEmpty() || !modified) {
            return;
        }

        var node = loader.createNode();

        npcs.forEach(sbaNPC -> {
            try {
                sbaNPC.destroy();
                node.appendListNode().set(sbaNPC);
            } catch (SerializationException ex) {
                ex.printStackTrace();
            }
        });

        try {
            loader.save(node);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }

        npcs.clear();
    }

    @OnEvent
    public void onPlayerJoin(SPlayerJoinEvent event) {
        final var player = event.getPlayer();

        Tasker.build(() -> npcs.forEach(sbaNPC -> {
                    var npc = sbaNPC.getNpc();
                    if (npc != null
                            && player.isOnline()) {
                        npc.addViewer(event.getPlayer());
                    }
                }))
                .delay(10, TaskerTime.TICKS)
                .start();
    }

    @OnEvent
    public void onPlayerLeave(SPlayerLeaveEvent event) {
        npcs.forEach(sbaNPC -> {
            var npc = sbaNPC.getNpc();
            if (npc != null) {
                npc.removeViewer(event.getPlayer());
            }
        });
    }

    @OnEvent
    public void onNPCInteract(NPCInteractEvent event) {
        npcs.stream()
                .filter(sbaNPC -> sbaNPC.getNpc().equals(event.getVisual()))
                .findFirst()
                .ifPresent(sbaNPC -> {
                    if (event.getInteractType() == InteractType.RIGHT_CLICK && NPCCommand.SELECTING_NPC.contains(event.getPlayer().getUuid())) {
                        NPCCommand.SELECTING_NPC.remove(event.getPlayer().getUuid());
                        NPCCommand.NPCS_IN_HAND.put(event.getPlayer().getUuid(), sbaNPC);
                        event.getPlayer().sendMessage(Message.of(LangKeys.ADMIN_NPC_EDITING)
                                .defaultPrefix()
                                .placeholder("x", sbaNPC.getLocation().getX(), 2)
                                .placeholder("y", sbaNPC.getLocation().getY(), 2)
                                .placeholder("z", sbaNPC.getLocation().getZ(), 2)
                                .placeholder("yaw", sbaNPC.getLocation().getYaw(), 5)
                                .placeholder("pitch", sbaNPC.getLocation().getPitch(), 5));
                    } else {
                        sbaNPC.handleClick(event.getPlayer().as(GamePlayer.class), event.getInteractType());
                    }
                });
    }
}
