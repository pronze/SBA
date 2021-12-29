package io.github.pronze.sba.service;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent;
import org.screamingsandals.bedwars.game.GameStore;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.npc.event.NPCInteractEvent;
import org.screamingsandals.lib.npc.skin.NPCSkin;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.utils.InteractType;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service(dependsOn = {
        GameManagerImpl.class
})
public final class NPCStoreService implements Listener {
    private final GameManagerImpl gameManager;
    private final SBAConfig config;

    @OnPostEnable
    public void onPostEnable(SBA plugin) {
        plugin.registerListener(this);
    }

    @EventHandler
    public void onBedWarsGameStartedEvent(BedwarsGameStartedEvent event) {
        final var game = event.getGame();
        final var maybeWrapper = gameManager.getWrappedGame(game);
        if (maybeWrapper.isEmpty()) {
            return;
        }

        final var gameWrapper = maybeWrapper.get();

        if (config.node("npc-stores", "enabled").getBoolean(true)) {
            gameWrapper.getGameStoreData().forEach(gameStoreData -> {
                final var npc = NPC.of(gameStoreData.getLocation());
                final var npcNode = gameStoreData.getNode();

                try {
                    final var displayName = Objects.requireNonNull(npcNode.node("display-name").getList(String.class))
                            .stream()
                            .map(AdventureHelper::toComponent)
                            .map(TextComponent::asComponent)
                            .collect(Collectors.toList());

                    npc.setDisplayName(displayName);
                } catch (SerializationException ex) {
                    ex.printStackTrace();
                }

                npc.setShouldLookAtPlayer(npcNode.node("look-at-player").getBoolean(true));
                npc.setTouchable(true);

                NPCSkin.retrieveSkin(npcNode.node("skin").getString()).thenAccept(npcSkin -> {
                    npc.setSkin(npcSkin);
                    game.getConnectedPlayers()
                            .stream()
                            .map(PlayerMapper::wrapPlayer)
                            .forEach(npc::addViewer);
                    npc.show();
                });

                gameWrapper.registerStoreNPC(gameStoreData, npc);
            });
        }
    }

    @OnEvent
    public void onNPCTouched(NPCInteractEvent event) {
        if (event.getInteractType() != InteractType.RIGHT_CLICK) {
            return;
        }

        final var player = event.getPlayer().as(Player.class);
        if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
            return;
        }

        final var game = Main.getInstance().getGameOfPlayer(player);
        final var npc = event.getVisual();
        gameManager
                .getRegisteredGames()
                .values()
                .forEach(gameWrapper -> gameWrapper.getRegisteredNPCS().forEach(((gameStoreData, npc1) -> {
                    if (npc1.equals(npc)) {
                        final var npcNode = gameStoreData.getNode();
                        final var store = new GameStore(null, npcNode.node("shop-file").getString(), false, npcNode.node("name").getString(), false, false);
                        BedwarsOpenShopEvent openShopEvent = new BedwarsOpenShopEvent(game,
                                player, store, null);
                        Bukkit.getServer().getPluginManager().callEvent(openShopEvent);
                    }
                })));
    }
}
