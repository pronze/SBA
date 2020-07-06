package org.pronze.hypixelify.listener;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.pronze.hypixelify.Hypixelify;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndEvent;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPreRebuildingEvent;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.game.GameStore;
import org.screamingsandals.bedwars.game.GamePlayer;
import java.util.Objects;

public class Shop implements Listener {

    public Shop(){
        Bukkit.getServer().getPluginManager().registerEvents(this, Hypixelify.getInstance());
        PlayerInteractEntityEvent.getHandlerList().unregister(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("BedWars")));
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
    public void onGameStarted(BedwarsGameStartedEvent e){
        for (GameStore store : Main.getGame(e.getGame().getName()).getGameStores()) {
            LivingEntity villager = store.kill();
            if (villager != null) {
                Main.unregisterGameEntity(villager);
            }
            NPC npc =  CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, store.getShopFile().replaceFirst("[.][^.]+$", ""));
            npc.spawn(store.getStoreLocation());
            npc.getTrait(LookClose.class).lookClose(true);
            if(npc.getName().contains("upgradeShop"))
                npc.getTrait(SkinTrait.class).setSkinName("Conefish");
            else
                npc.getTrait(SkinTrait.class).setSkinName("iamceph");
        }
}
    @EventHandler
    public void onRebuild(BedwarsPreRebuildingEvent e){
        CitizensAPI.getNPCRegistry().forEach(npc -> {
            if(Main.getGameNames().contains(npc.getStoredLocation().getWorld().getName())) {
                if (npc.getName().contains("shop") || npc.getName().contains("upgradeShop")) {
                    npc.despawn();
                }
            }
        });
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNPCLeftClick(NPCLeftClickEvent e) {
        e.setCancelled(onNPCClick(e.getClicker(), e.getNPC()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNPCRightClick(NPCRightClickEvent e) {
        e.setCancelled(onNPCClick(e.getClicker(), e.getNPC()));
    }
    private Boolean onNPCClick(Player player, NPC npc) {

        if(!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)){
            return false;
        }
        if(Main.getPlayerGameProfile(player).isSpectator){
            return false;
        }
        Game game = BedwarsAPI.getInstance().getGameOfPlayer(player);
        String shopName = "shop.yml";
        if((!npc.getName().contains("shop") && !npc.getName().contains("upgradeShop") )|| !game.isPlayerInAnyTeam(player)){
            return false;
        }
        if(npc.getName().contains("upgradeShop")){
            shopName = "upgradeShop.yml";
        }
        GameStore store = new GameStore(null, shopName, false,  "[BW] Shop",
                false, false);

        Hypixelify.getShop().show(player, store);

        return true;
    }


    @EventHandler
    public void gameOverEvent(BedwarsGameEndEvent e){
        CitizensAPI.getNPCRegistry().forEach(npc -> {

            if(npc.getStoredLocation().getWorld().getName().equalsIgnoreCase(e.getGame().getGameWorld().getName()))
            {
                if(npc.getName().contains("shop") || npc.getName().contains("upgradeShop")) {
                    npc.despawn();
                }
            }
        });
    }
    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin().getName().equals("BedWars")) {
          CitizensAPI.getNPCRegistry().forEach(npc -> {
              if (Main.getGameNames().contains(npc.getStoredLocation().getWorld().getName())) {
                  Bukkit.getLogger().info(npc.getName());
                  if (npc.getName().contains("shop") || npc.getName().contains("upgradeShop")){
                      npc.despawn();
              }
          }
          });
      }
    }
}
