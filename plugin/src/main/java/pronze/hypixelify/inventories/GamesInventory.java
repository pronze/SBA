package pronze.hypixelify.inventories;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.lib.debug.Debug;
import org.screamingsandals.bedwars.lib.sgui.SimpleInventories;
import org.screamingsandals.bedwars.lib.sgui.events.PostActionEvent;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.GamesInventoryOpenEvent;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.ShopUtil;

import java.io.File;
import java.util.HashMap;

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
    private final HashMap<Integer, SimpleInventories> inventoryMap = new HashMap<>();

    public void loadInventory() {
        try {
            labels.forEach((val, label) -> {
                try {
                    final var options = ShopUtil.generateOptions();
                    options.setPrefix(SBAHypixelify.getConfigurator().getString("games-inventory.gui." + label.toLowerCase() + "-prefix"));
                    final var siFormat = new SimpleInventories(options);
                    siFormat.loadFromDataFolder(new File(SBAHypixelify.getInstance().getDataFolder() + "/games-inventory"), label.toLowerCase() + ".yml");
                    inventoryMap.put(val, siFormat);
                    siFormat.generateData();
                    Logger.trace("Successfully loaded games inventory for: {}", label);
                } catch (Throwable T) {
                    Logger.trace("Could not initialize shop format for {}", label);
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
            player.closeInventory();
            format.openForPlayer(player);
        }
    }

    @EventHandler
    public void onPostAction(PostActionEvent event) {
        if (!inventoryMap.containsValue(event.getFormat())) {
            return;
        }

        final var mode = inventoryMap.keySet()
                .stream()
                .filter(key -> event.getFormat() == inventoryMap.get(key))
                .findFirst()
                .orElse(1);

        final var stack = event.getItem().getStack();
        final var player = event.getPlayer();
        final var reader = event.getItem().getReader();

        if (stack != null) {
            if (reader.containsKey("properties")) {
                final var property = reader.getString("properties");
                switch (property.toLowerCase()) {
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
            }
        }
        if (reader.containsKey("game")) {
            try {
                final var game = (Game) Main.getGame(reader.getString("game"));
                Main.getGame(game.getName()).joinToGame(player);
            } catch (Throwable T) {
                player.sendMessage(i18n("game_not_found"));
            }
            player.closeInventory();
        }
    }


}
