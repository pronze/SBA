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
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPreRebuildingEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.game.GameStore;
import org.screamingsandals.bedwars.game.GamePlayer;
import java.util.Objects;

public class Shop implements Listener {

    private final String ITEM_SHOP_NAME = "§bITEM SHOP";
    private final String UPGRADE_SHOP_NAME = "§6TEAM UPGRADES";
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
    public void onGameStart(BedwarsGameStartedEvent e){
        Game game = e.getGame();
        ShopUtil.RemoveNPCFromGame(game);

        for (GameStore store : Main.getGame(game.getName()).getGameStores()) {
            if (store.getShopFile() != null && (store.getShopFile().equalsIgnoreCase("shop.yml")
                    || store.getShopFile().equalsIgnoreCase("upgradeShop.yml"))) {
                LivingEntity villager = store.kill();
                if (villager != null) {
                    Main.unregisterGameEntity(villager);
                }
                String ShopName = store.getShopFile().replaceFirst("[.][^.]+$", "");
                if (ShopName.equalsIgnoreCase("shop")) {
                    ShopName = ITEM_SHOP_NAME;
                } else {
                    ShopName = UPGRADE_SHOP_NAME;
                }
                NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, ShopName);

                npc.spawn(store.getStoreLocation());
                npc.getTrait(LookClose.class).lookClose(true);
                if (npc.getName().contains(UPGRADE_SHOP_NAME))
                    npc.getTrait(SkinTrait.class).setSkinName("Conefish");
                else
                    npc.getTrait(SkinTrait.class).setSkinName("daddieskitten");
            }
        }
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
        ShopUtil.RemoveNPCFromGame(e.getGame());
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
            ShopUtil.destroyNPCFromGameWorlds();
      }
    }


}
