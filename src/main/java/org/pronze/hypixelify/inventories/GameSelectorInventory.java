package org.pronze.hypixelify.inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.pronze.hypixelify.Hypixelify;
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
import java.util.List;

import static org.pronze.hypixelify.utils.HypixelifyUtil.getHighestCount;
import static org.screamingsandals.bedwars.lib.lang.I.i18n;

public class GameSelectorInventory implements Listener {

    private SimpleInventories solos;
    private SimpleInventories doubles;
    private SimpleInventories triples;
    private SimpleInventories squads;
    private Options options;
    private List<Player> players = new ArrayList<>();

    public GameSelectorInventory(){
        Bukkit.getPluginManager().registerEvents(this, Hypixelify.getInstance());
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

        options.setRender_header_start(0);
        options.setRender_footer_start(45);

        options.setRender_offset(9);
        options.setRows(4);
        options.setRender_actual_rows(6);
    }

    public FormatBuilder retBuilder(Game game)
    {
        FormatBuilder builder = new FormatBuilder();
        ItemStack Available = new ItemStack(Material.BLACK_WOOL);
        ItemStack Running = new ItemStack(Material.RED_WOOL);
        ItemStack Disabled = new ItemStack(Material.CYAN_WOOL);

        if(game.getStatus().equals(GameStatus.WAITING))
            builder.add(Available).set("game", game);
        else if(game.getStatus().equals(GameStatus.RUNNING))
            builder.add(Running).set("game", game);
        else if(game.getStatus().equals(GameStatus.DISABLED))
            builder.add(Disabled).set("game", game);
        
        return builder;
    }
    

    private void createGUI(String mode){
        SimpleInventories solos = new SimpleInventories(options);
        SimpleInventories doubles = new SimpleInventories(options);
        SimpleInventories triples = new SimpleInventories(options);
        SimpleInventories squads = new SimpleInventories(options);

        FormatBuilder builder = null;

        for(Game game : BedwarsAPI.getInstance().getGames()){
            if(mode.equalsIgnoreCase("Solo") && game.getAvailableTeams().get(0).getMaxPlayers() == 1){
                builder = retBuilder(game);
                solos.load(builder);
                solos.generateData();
                this.solos = solos;
            }
            else if(mode.equalsIgnoreCase("Double") && game.getAvailableTeams().get(0).getMaxPlayers() == 2){
                builder = retBuilder(game);
                doubles.load(builder);
                doubles.generateData();
                this.doubles = doubles;
            }
            else if(mode.equalsIgnoreCase("Triple") && game.getAvailableTeams().get(0).getMaxPlayers() == 3){
                builder = retBuilder(game);
                triples.load(builder);
                triples.generateData();
                this.triples = triples;
            }
            else if(mode.equalsIgnoreCase("Squad") && game.getAvailableTeams().get(0).getMaxPlayers() == 4)
                builder = retBuilder(game);
                squads.load(builder);
                squads.generateData();
                this.squads= squads;
        }

    }

    public void showGUI(Player player, String mode){
        createGUI(mode);
        if(mode.equalsIgnoreCase("Solo"))
            solos.openForPlayer(player);
        else if(mode.equalsIgnoreCase("Double"))
            doubles.openForPlayer(player);
        else if(mode.equalsIgnoreCase("Triple"))
            triples.openForPlayer(player);
        else if(mode.equalsIgnoreCase("Squad"))
            squads.openForPlayer(player);

        players.add(player);
    }

    public void repaintSolo() {
        for (Player player : players) {
            GuiHolder guiHolder = solos.getCurrentGuiHolder(player);
            if (guiHolder == null) {
                return;
            }

            guiHolder.setFormat(solos);
            guiHolder.repaint();
        }
    }
    public void repaintDouble() {
        for (Player player : players) {
            GuiHolder guiHolder = doubles.getCurrentGuiHolder(player);
            if (guiHolder == null) {
                return;
            }

            guiHolder.setFormat(doubles);
            guiHolder.repaint();
        }
    }
    public void repaintTriples() {
        for (Player player : players) {
            GuiHolder guiHolder = triples.getCurrentGuiHolder(player);
            if (guiHolder == null) {
                return;
            }

            guiHolder.setFormat(triples);
            guiHolder.repaint();
        }
    }
    public void repaintSquad() {
        for (Player player : players) {
            GuiHolder guiHolder = squads.getCurrentGuiHolder(player);
            if (guiHolder == null) {
                return;
            }
            guiHolder.setFormat(squads);
            guiHolder.repaint();
        }
    }

    @EventHandler
    public void onPostAction(PostActionEvent event) {
        if (event.getFormat() != solos && event.getFormat() != doubles
        && event.getFormat() != triples && event.getFormat() != squads) {
            return;
        }

        Player player = event.getPlayer();
        MapReader reader = event.getItem().getReader();
        if (reader.containsKey("game")) {
            Game game = (Game) reader.get("game");
            Main.getGame(game.getName()).joinToGame(player);
            player.closeInventory();
            players.remove(player);

            if(event.getFormat().equals(solos))
                repaintSolo();
            else if(event.getFormat().equals(doubles))
                repaintDouble();
            else if(event.getFormat().equals(triples))
                repaintTriples();
            else if(event.getFormat().equals(squads))
                repaintSquad();
        }
    }
}
