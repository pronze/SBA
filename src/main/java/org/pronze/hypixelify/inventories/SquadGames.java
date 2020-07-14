package org.pronze.hypixelify.inventories;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.pronze.hypixelify.Configurator;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
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
import java.util.HashMap;
import java.util.List;

import static org.screamingsandals.bedwars.lib.lang.I.i18n;

public class SquadGames implements Listener {
    private SimpleInventories menu;
    private Options options;
    private List<Player> Players = new ArrayList<>();

    public SquadGames() {
        options = new Options(Hypixelify.getInstance());
        options.setShowPageNumber(true);

        ItemStack backItem = Main.getConfigurator().readDefinedItem("shopback", "BARRIER");
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName(i18n("shop_back", false));
        backItem.setItemMeta(backItemMeta);
        options.setBackItem(backItem);

        ItemStack pageBackItem = Main.getConfigurator().readDefinedItem("pageback", "ARROW");
        ItemMeta pageBackItemMeta = backItem.getItemMeta();
        pageBackItemMeta.setDisplayName(i18n("page_back", false));
        pageBackItem.setItemMeta(pageBackItemMeta);
        options.setPageBackItem(pageBackItem);

        ItemStack pageForwardItem = Main.getConfigurator().readDefinedItem("pageforward", "ARROW");
        ItemMeta pageForwardItemMeta = backItem.getItemMeta();
        pageForwardItemMeta.setDisplayName(i18n("page_forward", false));
        pageForwardItem.setItemMeta(pageForwardItemMeta);
        options.setPageForwardItem(pageForwardItem);

        ItemStack cosmeticItem = Main.getConfigurator().readDefinedItem("shopcosmetic", "AIR");
        options.setCosmeticItem(cosmeticItem);
        options.setRender_header_start(45);
        options.setRender_offset(9);
        options.setRows(4);
        options.setRender_actual_rows(4);
        options.setShowPageNumber(false);
        options.setPrefix("Bed Wars Squads");
        Bukkit.getServer().getPluginManager().registerEvents(this, Hypixelify.getInstance());
        createData();
    }

    private void createData() {
        SimpleInventories menu = new SimpleInventories(options);
        FormatBuilder builder = new FormatBuilder();
        ItemStack category = new ItemStack(Material.valueOf("RED_BED"));
        ItemStack category2 = new ItemStack(Material.OAK_SIGN);
        ItemStack category3 = new ItemStack(Material.BARRIER);
        ItemStack category4 = new ItemStack(Material.ENDER_PEARL);

        ItemMeta meta = category.getItemMeta();
        meta.setLore(Arrays.asList("§7Play Bed Wars squads", " ", "§eClick to play!"));
        String name = "&aBed Wars (Squads)";
        name = ChatColor.translateAlternateColorCodes('&', name);
        meta.setDisplayName(name);
        category.setItemMeta(meta);

        ItemMeta meta2 = category2.getItemMeta();
        String name2 = "&aMap Selector (Squads)";
        meta2.setLore(Arrays.asList("§7Pick which map you want to play", "§7from a list of available servers.", " "
        , "§eClick to browse!"));
        name2 = ChatColor.translateAlternateColorCodes('&', name2);
        meta2.setDisplayName(name2);
        category2.setItemMeta(meta2);

        ItemMeta meta3 = category3.getItemMeta();
        String name3 = "§cExit";
        meta3.setDisplayName(name3);
        category3.setItemMeta(meta3);

        ItemMeta meta4 = category4.getItemMeta();
        String name4 = "§cClick here to rejoin!";
        meta4.setLore(Arrays.asList("§7Click here to rejoin the lastly joined game"));
        meta4.setDisplayName(name4);
        category4.setItemMeta(meta4);

        ArrayList<Object> soloGames = new ArrayList<>();

        ItemStack air = new ItemStack(Material.AIR);
        HashMap<String, Object> tempmappings1 = new HashMap<>();
        tempmappings1.put("stack", air);
        soloGames.add(tempmappings1);
        for (Game game : BedwarsAPI.getInstance()
                .getGames()) {
            if (Configurator.game_size.containsKey(game.getName()) && Configurator.game_size.get(game.getName()).equals(4)) {
                ItemStack temp = new ItemStack(Material.PAPER);
                ItemMeta meta1 = temp.getItemMeta();
                String name1 = ChatColor.GREEN + game.getName();
                meta1.setLore(Arrays.asList("§8Squad", "", "§7Available Servers: §a1", "§7Status: §a{status}".replace("{status}", game.getStatus().name())
                ,"", "§aClick to play", "§eRight click to toggle favorite!"));
                meta1.setDisplayName(name1);
                temp.setItemMeta(meta1);
                HashMap<String, Object> tempmappings = new HashMap<>();
                tempmappings.put("stack", temp);
                tempmappings.put("game", game);
                soloGames.add(tempmappings);
            }
        }
        builder.add(category)
                .set("column", 3)
                .set("row", 1);
        builder.add(category2)
                .set("row", 1)
                .set("column", 5)
                .set("items", soloGames);
        builder.add(category3)
                .set("row", 3)
                .set("column", 4);
        builder.add(category4)
                .set("row",3)
                .set("column", 8);

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
            } else if(event.getItem().getStack().getType().equals(Material.RED_BED)){
                player.closeInventory();
                repaint();
                Players.remove(player);
                List<Game> games = ShopUtil.getGamesWithSize(4);
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
