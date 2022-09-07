package io.github.pronze.sba.inventories;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.events.SBAGamesInventoryOpenEvent;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.simpleinventories.SimpleInventoriesCore;
import org.screamingsandals.simpleinventories.events.PostClickEvent;
import org.screamingsandals.simpleinventories.inventory.Include;
import org.screamingsandals.simpleinventories.inventory.InventorySet;
import org.screamingsandals.simpleinventories.inventory.Property;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service(dependsOn = {
        SBAConfig.class,
        SimpleInventoriesCore.class
})
public class GamesInventory implements Listener {

    public static GamesInventory getInstance() {
        return ServiceManager.get(GamesInventory.class);
    }

    private final static HashMap<Integer, String> labels = new HashMap<>() {
        {
            put(1, "solo");
            put(2, "double");
            put(3, "triples");
            put(4, "squads");
        }
    };

    private final HashMap<String, InventorySet> inventoryMap = new HashMap<>();

    public List<String> getGameModeNames() {
        return new ArrayList<>(inventoryMap.keySet());
    }

    @OnPostEnable
    public void loadInventory() {
        try {
            Arrays.stream(new File(SBA.getPluginInstance().getDataFolder().getAbsolutePath() + "/games-inventory/")
                    .listFiles())
                    .forEach(inventoryFile -> {
                        try {
                            String label = inventoryFile.getName().toLowerCase().replace(".yml", "");
                            if (label.equals("npc"))
                                return;
                            final var siFormat = SimpleInventoriesCore.builder()
                                    .categoryOptions(localOptionsBuilder -> {
                                        ShopUtil.generateOptions(localOptionsBuilder);
                                        try {
                                            localOptionsBuilder.prefix(LanguageService.getInstance()
                                                    .get("games-inventory", "gui", label.toLowerCase() + "-prefix")
                                                    .toString());
                                        } catch (Throwable t) {
                                            localOptionsBuilder.prefix(label);
                                        }
                                    })
                                    .call(categoryBuilder -> {
                                        try {
                                            categoryBuilder
                                                    .include(Include.of(Paths.get(inventoryFile.getAbsolutePath())));
                                        } catch (Throwable t) {
                                            // t.printStackTrace();
                                        }
                                    })
                                    .click(this::onClick)
                                    .process()
                                    .getInventorySet();

                            inventoryMap.put(label, siFormat);
                            Logger.trace("Successfully loaded games inventory for: {}", inventoryFile.getName());
                        } catch (Throwable t) {
                            // Logger.trace("Could not initialize games inventory format for {}",
                            // inventoryFile.getName());
                            // t.printStackTrace();
                        }
                    });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openForPlayer(Player player, int mode) {
        final var event = new SBAGamesInventoryOpenEvent(player, labels.get(mode));
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        final var format = inventoryMap.get(labels.get(mode));
        if (format != null) {
            PlayerMapper.wrapPlayer(player).openInventory(format);
        }
    }

    public void openForPlayer(Player player, String mode) {
        final var event = new SBAGamesInventoryOpenEvent(player, mode);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        final var format = inventoryMap.get(mode);
        if (format != null) {
            PlayerMapper.wrapPlayer(player).openInventory(format);
        }
    }

    public static List<Game> getGamesWithSize(int size) {
        return Main.getGameNames().stream().map(g -> Main.getGame(g))
                .filter(g -> g.getAvailableTeams().stream().allMatch(t -> t.getMaxPlayers() == size))
                .collect(Collectors.toList());
    }

    public List<Game> getGamesWithMode(String mode) {
        InventorySet invSet = inventoryMap.get(mode);
        if (invSet == null)
            return List.of();
        List<Game> ret = new ArrayList<>();
        var subInventory = invSet.getMainSubInventory();
        for (var inventoryParent : subInventory.getContents()) {
            if (inventoryParent.getChildInventory() != null)
                for (var inventoryParent2 : inventoryParent.getChildInventory().getContents()) {
                    if (inventoryParent2.getProperties().stream().anyMatch(x -> x.getPropertyName().equals("join"))) {
                        String gameName = (inventoryParent2.getFirstPropertyByName("join").orElseThrow()
                                .getPropertyData().node("gameName").getString());
                        if (gameName != null) {
                            Game game = Main.getInstance().getGameByName(gameName);
                            if (game != null) {
                                ret.add(game);
                            }
                        }
                    }

                }
            if (inventoryParent.getProperties().stream().anyMatch(x -> x.getPropertyName().equals("join"))) {
                String gameName = (inventoryParent.getFirstPropertyByName("join").orElseThrow()
                        .getPropertyData().node("gameName").getString());
                if (gameName != null) {
                    Game game = Main.getInstance().getGameByName(gameName);
                    if (game != null) {
                        ret.add(game);
                    }
                }
            }

        }
        return ret;
    }

    public void onClick(PostClickEvent event) {
        final var mode = inventoryMap.keySet()
                .stream()
                .filter(key -> event.getFormat() == inventoryMap.get(key))
                .findFirst()
                .orElse(labels.get(1));

        final var item = event.getItem();
        final var stack = item.getStack();
        final var player = event.getPlayer().as(Player.class);
        final var properties = item.getProperties();

        if (stack != null) {
            if (item.hasProperties()) {
                var couldNotFindGameMessage = LanguageService
                        .getInstance()
                        .get(MessageKeys.GAMES_INVENTORY_CANNOT_FIND_GAME);
                final var playerWrapper = PlayerMapper.wrapPlayer(player);

                player.closeInventory();
                properties.stream()
                        .filter(Property::hasName)
                        .forEach(property -> {
                            switch (property.getPropertyName().toLowerCase()) {
                                case "exit":
                                    break;
                                case "randomly_join":
                                    final var games = getGamesWithMode(mode);
                                    if (games == null || games.isEmpty()) {
                                        couldNotFindGameMessage.send(playerWrapper);
                                        return;
                                    }

                                    Random r = new Random();
                                    games.sort(Comparator.comparing(c -> ((Game) c).getConnectedPlayers().size())
                                            .reversed().thenComparing(c -> r.nextInt()));

                                    games.stream()
                                            .filter(game -> game.getStatus() == GameStatus.WAITING)
                                            .findAny()
                                            .ifPresentOrElse(game -> game.joinToGame(player),
                                                    () -> couldNotFindGameMessage.send(playerWrapper));
                                    break;
                                case "rejoin":
                                    player.performCommand("bw rejoin");
                                    break;
                                case "join":
                                    final var gameName = item.getFirstPropertyByName("join").orElseThrow()
                                            .getPropertyData().node("gameName").getString();
                                    if (gameName == null) {
                                        couldNotFindGameMessage.send(playerWrapper);
                                        return;
                                    }
                                    Main.getInstance().getGameByName(gameName).joinToGame(player);
                                    break;
                                default:
                                    break;
                            }
                        });
            }
        }
    }

}
