package pronze.hypixelify.inventories;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.lib.debug.Debug;
import org.screamingsandals.bedwars.lib.sgui.SimpleInventories;
import org.screamingsandals.bedwars.lib.sgui.events.PostActionEvent;
import org.screamingsandals.bedwars.lib.sgui.inventory.Options;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.events.GamesInventoryOpenEvent;
import pronze.hypixelify.utils.Logger;
import pronze.hypixelify.utils.ShopUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static pronze.hypixelify.lib.lang.I.i18n;

public class GamesInventory implements Listener {
    private final HashMap<Integer, SimpleInventories> inventoryMap = new HashMap<>();
    private final HashMap<Integer, Options> option = new HashMap<>();
    private final HashMap<Integer, List<Player>> players = new HashMap<>();
    private final HashMap<Integer, String> labels = new HashMap<>();

    public GamesInventory() {
        String soloprefix, doubleprefix, tripleprefix, squadprefix;

        soloprefix = SBAHypixelify.getConfigurator().getString("games-inventory.gui.solo-prefix");
        doubleprefix = SBAHypixelify.getConfigurator().getString("games-inventory.gui.double-prefix");
        tripleprefix = SBAHypixelify.getConfigurator().getString("games-inventory.gui.triple-prefix");
        squadprefix = SBAHypixelify.getConfigurator().getString("games-inventory.gui.squad-prefix");

        final var option1 = ShopUtil.generateOptions();
        option1.setPrefix(soloprefix);
        option.put(1, option1);
        final var option2 = ShopUtil.generateOptions();
        option2.setPrefix(doubleprefix);
        option.put(2, option2);
        final var option3 = ShopUtil.generateOptions();
        option3.setPrefix(tripleprefix);
        option.put(3, option3);
        final var option4 = ShopUtil.generateOptions();
        option4.setPrefix(squadprefix);
        option.put(4, option4);

        labels.put(1, "Solo");
        labels.put(2, "Double");
        labels.put(3, "Triple");
        labels.put(4, "Squad");

        Bukkit.getServer().getPluginManager().registerEvents(this, SBAHypixelify.getInstance());
        loadInventory();
    }

    private void loadInventory() {
        //hmm?
        //TODO: test
        try {
            labels.forEach((val, label) -> {
                try {
                    final var siFormat = new SimpleInventories(option.get(val));
                    siFormat.loadFromDataFolder(SBAHypixelify.getInstance().getDataFolder(), label.toLowerCase() + ".yml");
                    inventoryMap.put(val, siFormat);
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

    public void destroy() {
        players.clear();
        HandlerList.unregisterAll(this);
    }

    public void openForPlayer(Player player, int mode) {
        final var event = new GamesInventoryOpenEvent(player, mode);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        if (inventoryMap.get(mode) == null)
            return;
        inventoryMap.get(mode).openForPlayer(player);
        players.computeIfAbsent(mode, k -> new ArrayList<>());
        players.get(mode).add(player);
    }

    public void repaint(int mode) {
        for (Player player : players.get(mode)) {
            var guiHolder = inventoryMap.get(mode).getCurrentGuiHolder(player);
            if (guiHolder == null) {
                return;
            }
            guiHolder.setFormat(inventoryMap.get(mode));
            guiHolder.repaint();
        }
    }

    @EventHandler
    public void onPostAction(PostActionEvent event) {
        if (event.getFormat() != inventoryMap.get(1) &&
                event.getFormat() != inventoryMap.get(2) &&
                event.getFormat() != inventoryMap.get(3) &&
                event.getFormat() != inventoryMap.get(4)) {
            return;
        }

        int mode = event.getFormat() == inventoryMap.get(1) ? 1 :
                event.getFormat() == inventoryMap.get(2) ? 2 :
                        event.getFormat() == inventoryMap.get(3) ? 3 :
                                event.getFormat() == inventoryMap.get(4) ? 4 : 1;

        final var stack = event.getItem().getStack();
        final var player = event.getPlayer();
        final var reader = event.getItem().getReader();

        if (stack != null) {
            if (reader.containsKey("properties")) {
                final var property = reader.getString("properties");
                switch (property.toLowerCase()) {
                    case "exit":
                        players.get(mode).remove(player);
                        player.closeInventory();
                        break;
                    case "join_randomly":
                        player.closeInventory();
                        repaint(mode);
                        players.get(mode).remove(player);
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
                        repaint(mode);
                        players.get(mode).remove(player);
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
                i18n("game_not_found");
            }
            player.closeInventory();
            repaint(mode);
            players.get(mode).remove(player);
        }
    }


}
