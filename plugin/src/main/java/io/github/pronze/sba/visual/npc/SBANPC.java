package io.github.pronze.sba.visual.npc;

import io.github.pronze.sba.SBWAddonAPI;
import io.github.pronze.sba.game.GamePlayer;
import io.github.pronze.sba.util.SerializableLocation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.Server;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.npc.skin.NPCSkin;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.InteractType;
import org.screamingsandals.lib.utils.TriConsumer;
import org.screamingsandals.lib.world.LocationHolder;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// modified from bw 0.3.x
@RequiredArgsConstructor
@Data
@ConfigSerializable
public final class SBANPC {
    private transient NPC npc;
    private SerializableLocation location;
    private NPCSkin skin;
    private String value;
    private Action action;

    private boolean shouldLookAtPlayer = true;
    private final List<Component> hologramAbove = new ArrayList<>();

    public void spawn() {
        if (npc == null
                && location != null
                && location.isWorldLoaded()) {
            npc = NPC.of(location.as(LocationHolder.class))
                    .setDisplayName(hologramAbove)
                    .setTouchable(true)
                    .setShouldLookAtPlayer(shouldLookAtPlayer);

            if (skin != null
                    && skin.getValue() != null) {
                npc.setSkin(skin);
            }

            npc.show();
            Server.getConnectedPlayers().forEach(npc::addViewer);
        }
    }

    public void handleClick(@NotNull GamePlayer player, @NotNull InteractType type) {
        if (action != null) {
            action.handler.accept(this, player, type);
        }
    }

    public void destroy() {
        npc.destroy();
    }

    @RequiredArgsConstructor
    public enum Action {
        DUMMY((sbaNPC, playerWrapper, type) -> {
        }),

        OPEN_GAMES_INVENTORY((sbaNPC, playerWrapper, interactType) -> {
            Server.runSynchronously(() -> SBWAddonAPI.getInstance().getGamesInventory().openForPlayer(playerWrapper, sbaNPC.getValue()));
        }),

        PLAYER_COMMAND((sbaNPC, playerWrapper, type) -> {
            Server.runSynchronously(() -> playerWrapper.tryToDispatchCommand(sbaNPC.getValue()));
        }),

        CONSOLE_COMMAND((sbaNPC, playerWrapper, type) -> {
            Server.runSynchronously(() -> PlayerMapper.getConsoleSender().tryToDispatchCommand(sbaNPC.getValue()));
        }),

        JOIN_GAME((sbaNPC, playerWrapper, type) -> {
            Server.runSynchronously(() -> Optional
                    .ofNullable(Main.getInstance().getGameByName(sbaNPC.getValue()))
                    .ifPresent(game -> game.joinToGame(playerWrapper.as(Player.class))));
        });


        private final TriConsumer<SBANPC, GamePlayer, InteractType> handler;

        public boolean requireArguments() {
            return this != DUMMY;
        }
    }
}
