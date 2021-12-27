package io.github.pronze.sba.inventory;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.event.GamesInventoryOpenEvent;
import io.github.pronze.sba.game.GamePlayer;
import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.lang.LangKeys;
import io.github.pronze.sba.service.GameManagerImpl;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.parameters.DataFolder;
import org.screamingsandals.lib.utils.logger.LoggerWrapper;
import org.screamingsandals.simpleinventories.SimpleInventoriesCore;
import org.screamingsandals.simpleinventories.events.PostClickEvent;
import org.screamingsandals.simpleinventories.inventory.GenericItemInfo;
import org.screamingsandals.simpleinventories.inventory.Include;
import org.screamingsandals.simpleinventories.inventory.InventorySet;
import org.screamingsandals.simpleinventories.inventory.Property;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public final class GamesInventoryImpl implements GamesInventory {
    @DataFolder("games-inventory")
    private final Path gamesInventoryFolder;
    private final LoggerWrapper logger;
    private final SBA plugin;
    private final GameManagerImpl gameManager;

    private final Map<String, InventorySet> inventoryMap = new HashMap<>();

    @OnPostEnable
    public void loadInventory() {
        if (!Files.exists(gamesInventoryFolder)) {
            try {
                Files.createDirectory(gamesInventoryFolder);
                plugin.saveResource("games-inventory/example-games-inventory.yml.disabled", false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }


        try (var stream = Files.walk(gamesInventoryFolder.toAbsolutePath())) {
            final var results = stream.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            if (results.isEmpty()) {
                logger.debug("No games inventories have been found!");
                return;
            }

            results.forEach(file -> {
                if (file.isFile()
                        && !file.getName().toLowerCase().endsWith(".disabled")) {
                    final var dot = file.getName().indexOf(".");
                    final var name = dot == -1 ? file.getName() : file.getName().substring(0, dot);
                    final var siFormat = SimpleInventoriesCore.builder()
                            .categoryOptions(localOptionsBuilder -> {
                                localOptionsBuilder
                                        .renderHeaderStart(600)
                                        .renderFooterStart(600)
                                        .renderOffset(9)
                                        .rows(4)
                                        .renderActualRows(4)
                                        .showPageNumber(false)
                                        .prefix(name); // TODO: translatable?
                            })
                            .call(categoryBuilder -> {
                                try {
                                    categoryBuilder.include(Include.of(file));
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            })
                            .click(this::onClick)
                            .process()
                            .getInventorySet();

                    inventoryMap.put(name, siFormat);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean openForPlayer(@NotNull PlayerWrapper player, @NotNull String type) {
        final var format = inventoryMap.get(type);
        if (format == null) {
            return false;
        }

        final var event = new GamesInventoryOpenEvent(player.as(GamePlayer.class), type);
        EventManager.fire(event);
        if (event.isCancelled()) {
            return false;
        }
        player.openInventory(format);
        return true;
    }

    @Override
    @NotNull
    public List<String> getInventoryNames() {
        return List.copyOf(inventoryMap.keySet());
    }

    private void onClick(PostClickEvent event) {
        final var item = event.getItem();
        final var stack = item.getStack();
        final var player = event.getPlayer();
        final var properties = item.getProperties();

        if (stack == null || !item.hasProperties()) {
            return;
        }

        player.closeInventory();
        properties
                .stream()
                .filter(Property::hasName)
                .forEach(property -> {
                    final var propertyName = property.getPropertyName().toLowerCase();
                    switch (propertyName) {
                        case "randomly_join":
                            // TODO: Manual registration of game modes into configuration instead of configuring them only via gamesinv.

                            final var randomlyJoin = item.getFirstPropertyByName("randomly_join").orElseThrow();
                            final var games = randomlyJoin.getPropertyData().node("games");
                            final var gameList = new ArrayList<GameWrapper>();

                            if (!games.isList()) {
                                try {
                                    var list = event.getSubInventory().getContents().stream()
                                            .map(genericItemInfo -> genericItemInfo.getFirstPropertyByName("join"))
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .map(property1 -> property1.getPropertyData().node("gameName").getString())
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList());

                                    if (list.isEmpty()) {
                                        // probably all the games are in some subinv
                                        list = event.getSubInventory()
                                                .getContents()
                                                .stream()
                                                .filter(GenericItemInfo::hasChildInventory)
                                                .map(GenericItemInfo::getChildInventory)
                                                .filter(Objects::nonNull)
                                                .flatMap(childInventory -> childInventory.getContents().stream())
                                                .map(genericItemInfo -> genericItemInfo.getFirstPropertyByName("join"))
                                                .filter(Optional::isPresent)
                                                .map(Optional::get)
                                                .map(property1 -> property1.getPropertyData().node("gameName").getString())
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toList());
                                    }
                                    games.set(list);
                                } catch (SerializationException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            games.childrenList().forEach(configurationNode ->
                                    gameManager.getWrappedGame(configurationNode.getString("")).ifPresent(gameList::add)
                            );
                            if (gameList.isEmpty()) {
                                player.sendMessage(Message.of(LangKeys.GAMES_INVENTORY_CANNOT_FIND_GAME).defaultPrefix());
                                return;
                            }
                            gameList.stream()
                                    .map(GameWrapper::getGame)
                                    .filter(game -> game.getStatus() == GameStatus.WAITING)
                                    .findAny()
                                    .ifPresentOrElse(
                                            game -> game.joinToGame(player.as(Player.class)),
                                            () -> player.sendMessage(Message.of(LangKeys.GAMES_INVENTORY_CANNOT_FIND_GAME).defaultPrefix())
                                    );

                            break;

                        case "rejoin":
                            player.as(Player.class).performCommand("bw rejoin");
                            break;

                        case "join":
                            final var gameName = item.getFirstPropertyByName("join").orElseThrow().getPropertyData().node("gameName").getString();
                            if (gameName == null) {
                                player.sendMessage(Message.of(LangKeys.GAMES_INVENTORY_CANNOT_FIND_GAME).defaultPrefix());
                                return;
                            }
                            gameManager.getWrappedGame(gameName).ifPresentOrElse(
                                    gameWrapper -> gameWrapper.getGame().joinToGame(player.as(Player.class)),
                                    () -> player.sendMessage(Message.of(LangKeys.GAMES_INVENTORY_CANNOT_FIND_GAME).defaultPrefix())
                            );
                    }
                });
    }

}
