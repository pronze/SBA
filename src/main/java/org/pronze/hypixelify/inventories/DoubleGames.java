package org.pronze.hypixelify.inventories;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.lib.sgui.SimpleInventories;
import org.screamingsandals.bedwars.lib.sgui.builder.FormatBuilder;
import org.screamingsandals.bedwars.lib.sgui.events.PostActionEvent;
import org.screamingsandals.bedwars.lib.sgui.inventory.GuiHolder;
import org.screamingsandals.bedwars.lib.sgui.inventory.Options;
import org.screamingsandals.bedwars.lib.sgui.utils.MapReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DoubleGames implements Listener {
    private SimpleInventories menu;
    private Options options;
    private List<Player> Players = new ArrayList<>();

    public DoubleGames() {
        options = ShopUtil.generateOptions();
        options.setPrefix("Bed Wars Double");
        Bukkit.getServer().getPluginManager().registerEvents(this, Hypixelify.getInstance());
        createData();
    }

    private void createData() {
        SimpleInventories menu = new SimpleInventories(options);
        List<ItemStack> myCategories = ShopUtil.createCategories(Arrays.asList("§7Play Bed Wars doubles", " ", "§eClick to play!"),
                "§aBed Wars (Double)","§aMap Selector (Double)");
        ItemStack category = myCategories.get(0);
        ItemStack category2 = myCategories.get(1);
        ItemStack category3 = myCategories.get(2);
        ItemStack category4 = myCategories.get(3);


        ArrayList<Object> doubleGames  = ShopUtil.createGamesGUI(2, Arrays.asList("§8Doubles", "", "§7Available Servers: §a1", "§7Status: §a{status}"
                ,"§7Players:§a {players}","", "§aClick to play", "§eRight click to toggle favorite!"));
        FormatBuilder builder = ShopUtil.createBuilder(doubleGames, category, category2, category3, category4);

        menu.load(builder);
        menu.generateData();

        this.menu = menu;
    }

    public void openForPlayer(Player player) {
        createData();
        menu.openForPlayer(player);
        Players.add(player);
    }

    public void repaint() {
        for (Player player : Players) {
            GuiHolder guiHolder = menu.getCurrentGuiHolder(player);
            if (guiHolder == null) {
                return;
            }

            createData();
            guiHolder.setFormat(menu);
            guiHolder.repaint();
        }
    }

    @EventHandler
    public void onPostAction(PostActionEvent event) {
        if (event.getFormat() != menu) {
            return;
        }

        Player player = event.getPlayer();
        if(event.getItem().getStack() != null)
        {
            if(event.getItem().getStack().getType().equals(Material.BARRIER)) {
                Players.remove(player);
                player.closeInventory();
            } else if(event.getItem().getStack().getType().equals(Material.RED_BED)
                    || event.getItem().getStack().getType().equals(Material.FIREWORK_ROCKET)
                    || event.getItem().getStack().getType().equals(Material.DIAMOND)){
                player.closeInventory();
                repaint();
                Players.remove(player);
                List<Game> games = ShopUtil.getGamesWithSize(2);
                if(games == null || games.isEmpty())
                    return;
                for (Game game : games){
                    if(game.getStatus().equals(GameStatus.WAITING)) {
                        game.joinToGame(player);
                        break;
                    }
                }
            } else  if(event.getItem().getStack().getType().equals(Material.ENDER_PEARL)){
                player.closeInventory();
                repaint();
                Players.remove(player);
                player.performCommand("bw rejoin");
        }
        }

        MapReader reader = event.getItem().getReader();
        if (reader.containsKey("game")) {
            Game game = (Game) reader.get("game");
            Main.getGame(game.getName()).joinToGame(player);
            player.closeInventory();

            repaint();
            Players.remove(player);
        }
    }
}
