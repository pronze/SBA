package org.pronze.hypixelify.listener;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.pronze.hypixelify.Hypixelify;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsPreRebuildingEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.game.GameStore;
import org.screamingsandals.bedwars.game.GameCreator;
import org.screamingsandals.bedwars.game.GamePlayer;
import java.util.ArrayList;
import java.util.Objects;

public class Shop implements Listener {

    public static final String ITEM_SHOP_NAME = "§bITEM SHOP";
    public static String UPGRADE_SHOP_NAME = "§6TEAM UPGRADES";
    GameStore shop;
    GameStore upgradeShop;

    public Shop(){
        Bukkit.getServer().getPluginManager().registerEvents(this, Hypixelify.getInstance());
        PlayerInteractEntityEvent.getHandlerList().unregister(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("BedWars")));
        shop = new GameStore(null, "shop.yml", false,  "[BW] Shop",
                false, false);
        upgradeShop = new GameStore(null, "upgradeShop.yml", false,  "[BW] Team Upgrades",
                false, false);
   }

    static public String capFirstLetter ( String str )
    {
        String firstLetter = str.substring(0,1).toUpperCase();
        String restLetters = str.substring(1).toLowerCase();
        return firstLetter + restLetters;
    }
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        if (Main.isPlayerInGame(player)) {
            GamePlayer gPlayer = Main.getPlayerGameProfile(player);
            Game game = gPlayer.getGame();
            if (game.getStatus() == GameStatus.WAITING || gPlayer.isSpectator) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRebuild(BedwarsPreRebuildingEvent e){
        ArrayList<NPC> npcs = new ArrayList<>();

        CitizensAPI.getNPCRegistry().forEach(npc -> {
            if(Main.getGameNames().contains(npc.getStoredLocation().getWorld().getName())) {
                if(GameCreator.isInArea(npc.getStoredLocation(), e.getGame().getPos1(), e.getGame().getPos2()))
                    npcs.add(npc);
            }
        });

        for(NPC npc : npcs){
            npc.destroy();
        }

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNPCLeftClick(NPCLeftClickEvent e) {
        if(!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(e.getClicker()))
            return;

        if((!e.getNPC().getName().contains(ITEM_SHOP_NAME) && !e.getNPC().getName().contains(UPGRADE_SHOP_NAME)))
            return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNPCRightClick(NPCRightClickEvent e) {
        e.setCancelled(onNPCClick(e.getClicker(), e.getNPC()));
    }
    private Boolean onNPCClick(Player player, NPC npc) {

        if(!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)){
            return true;
        }

        if(Main.getPlayerGameProfile(player).isSpectator){
            return true;
        }
        Game game = BedwarsAPI.getInstance().getGameOfPlayer(player);
        GameStore store = null;
        if((!npc.getName().contains(ITEM_SHOP_NAME) && !npc.getName().contains(UPGRADE_SHOP_NAME) )|| !game.isPlayerInAnyTeam(player)){
            return false;
        }
        if(npc.getName().contains(UPGRADE_SHOP_NAME)){
            store = upgradeShop;
        }
        else if(npc.getName().contains(ITEM_SHOP_NAME)){
            store = shop;
        }

        Hypixelify.getShop().show(player, store);

        return true;
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin().getName().equals("BedWars")) {
            ArrayList<NPC> npcs = new ArrayList<>();
            CitizensAPI.getNPCRegistry().forEach(npc -> {
              if (Main.getGameNames().contains(npc.getStoredLocation().getWorld().getName())) {
                  if (npc.getName().contains(ITEM_SHOP_NAME) || npc.getName().contains(UPGRADE_SHOP_NAME)){
                      npcs.add(npc);
              }
          }
          });
            for(NPC npc : npcs){
                npc.destroy();
            }
      }
    }


}
