package pronze.hypixelify.inventories;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.commands.DumpCommand;
import org.screamingsandals.bedwars.lib.debug.Debug;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.sgui.SimpleInventoriesCore;
import org.screamingsandals.bedwars.lib.sgui.events.PostClickEvent;
import org.screamingsandals.bedwars.lib.sgui.inventory.Include;
import org.screamingsandals.bedwars.lib.sgui.inventory.InventorySet;
import org.screamingsandals.bedwars.lib.sgui.inventory.Property;
import org.screamingsandals.bedwars.lib.utils.ConfigurateUtils;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.GamesInventoryOpenEvent;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.ShopUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static pronze.hypixelify.lib.lang.I.i18n;

public class GamesInventory implements Listener {
    private final static HashMap<Integer, String> labels = new HashMap<>() {
        {
            put(1, "solo");
            put(2, "double");
            put(3, "triple");
            put(4, "squad");
        }
    };
    private final HashMap<Integer, InventorySet> inventoryMap = new HashMap<>();

    public void loadInventory() {
        try {
            labels.forEach((val, label) -> {
                try {
                    final var siFormat = SimpleInventoriesCore.builder()
                            .categoryOptions(localOptionsBuilder -> {
                                ShopUtil.generateOptions(localOptionsBuilder);
                                localOptionsBuilder.prefix(SBAHypixelify.getConfigurator()
                                        .getString("games-inventory.gui." + label.toLowerCase() + "-prefix"));
                            })
                            .call(categoryBuilder -> categoryBuilder.include(Include
                                    .of(SBAHypixelify.getInstance().getDataFolder().toPath().resolve(
                                             "games-inventory/" + label.toLowerCase() + ".yml").toAbsolutePath())))
                            .click(this::onClick)
                            .process()
                            .getInventorySet();

                    inventoryMap.put(val, siFormat);
                    Logger.trace("Successfully loaded games inventory for: {}", label);
                } catch (Throwable T) {
                    Logger.trace("Could not initialize games inventory format for {}", label);
                }
            });
        } catch (Exception ex) {
            Debug.warn("Wrong GamesInventory configuration!", true);
            Debug.warn("Check validity of your YAML/Groovy!", true);
            ex.printStackTrace();
        }
    }

    public void openForPlayer(Player player, int mode) {
        final var event = new GamesInventoryOpenEvent(player, mode);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        final var format = inventoryMap.get(mode);
        if (format != null) {
            PlayerMapper.wrapPlayer(player).openInventory(format);
        }
    }

    public void onClick(PostClickEvent event) {
        final var mode = inventoryMap.keySet()
                .stream()
                .filter(key -> event.getFormat() == inventoryMap.get(key))
                .findFirst()
                .orElse(1);

        final var item = event.getItem();
        final var stack = item.getStack();
        final var player = event.getPlayer().as(Player.class);
        final var properties = item.getProperties();

        if (stack != null) {
            if (item.hasProperties()) {
                properties.stream()
                        .filter(Property::hasName)
                        .forEach(property -> {
                            switch (property.getPropertyName().toLowerCase()) {
                                case "exit":
                                    player.closeInventory();
                                    break;
                                case "join_randomly":
                                    player.closeInventory();
                                    final var games = ShopUtil.getGamesWithSize(mode);
                                    if (games == null || games.isEmpty())
                                        return;
                                    games.stream()
                                            .filter(game -> game.getStatus() == GameStatus.WAITING)
                                            .findAny()
                                            .ifPresent(game -> game.joinToGame(player));
                                    break;
                                case "rejoin":
                                    player.closeInventory();
                                    player.performCommand("bw rejoin");
                                    break;
                                default:
                                    break;
                            }

                            var converted = ConfigurateUtils.raw(property.getPropertyData());
                            if (!(converted instanceof Map)) {
                                converted = DumpCommand.nullValuesAllowingMap("value", converted);
                            }
                            final var propertyMap = (Map<?, ?>)converted;
                            if (propertyMap.containsKey("gameName")) {
                                try {
                                    final var game = (Game) Main.getGame(propertyMap.get("gameName").toString());
                                    Main.getGame(game.getName()).joinToGame(player);
                                } catch (Throwable t) {
                                    player.sendMessage(i18n("game_not_found"));
                                }
                                player.closeInventory();
                            }
                        });
            }
        }
    }


}
