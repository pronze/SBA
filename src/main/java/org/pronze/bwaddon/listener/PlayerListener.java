package org.pronze.bwaddon.listener;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.bukkit.inventory.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.screamingsandals.bedwars.api.game.*;
import org.screamingsandals.bedwars.api.TeamColor;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.utils.ColorChanger;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;





import org.bukkit.event.inventory.InventoryCloseEvent;

public class PlayerListener implements Listener{
	org.pronze.bwaddon.BwAddon plugin;
	private BedwarsAPI api;
	static public HashMap<Player, ItemStack[]> armor = new HashMap<Player, ItemStack[]>();
	static public HashMap<Player, List<ItemStack>> enchant = new HashMap<Player, List<ItemStack>>();
	
	public PlayerListener(org.pronze.bwaddon.BwAddon plugin)
	{
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	} 
	
	
	
	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent e)
	{

		api = BedwarsAPI.getInstance();
		

		Player player = e.getPlayer();
		
		Game game = api.getGameOfPlayer(player);
		
		Team team = game.getTeamOfPlayer(player);
        
        
        if(api.isPlayerPlayingAnyGame(player))
        {
        	new BukkitRunnable() {
                
                @Override
                public void run() {
                	if(player.getGameMode() == GameMode.SURVIVAL && api.isPlayerPlayingAnyGame(player)) {
            		giveItemsToPlayer(enchant.get(player), player, team.getColor());
            		if(armor.containsKey(player))
            			player.getInventory().setArmorContents(armor.get(player));
                	this.cancel();
                	}
                }
                
            }.runTaskTimer(this.plugin, 20L,20L);
        }
		
		
	}
	

	
	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent e)
	{

		api = BedwarsAPI.getInstance();
		
		if (!(e.getEntity() instanceof Player)) return;

		Player player = e.getEntity();
        
        Game game = api.getGameOfPlayer(player);
        
        if(api.isPlayerPlayingAnyGame(player))
        {       	
            //       armor.put(player, arcontent);
            
            List<ItemStack> enList = new ArrayList<>();
            ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
            
            
            for(ItemStack newItem : e.getDrops()) {
    			if(newItem.getType() == Material.WOODEN_SWORD | newItem.getType() == Material.STONE_SWORD || newItem.getType() == Material.IRON_SWORD || newItem.getType() == Material.DIAMOND_SWORD )
    			{
    				if(newItem.getEnchantments().size() > 0)
    				{
    					 sword.addEnchantments(newItem.getEnchantments());
    				}
    			}
        }
            
            if(enchant.containsKey(player))
    			enchant.remove(player);
            
            enList.add(sword);
    		enchant.put(player, enList);
    		
    		setArmor(player);
		
	  }
	}
	
	public static void setArmor(Player player)
	{
		if(armor.containsKey(player))
			armor.remove(player);
		
		armor.put(player, player.getInventory().getArmorContents());
	}
	
	/*public static void setEnchant(Player, List<ItemStack> itemStack)
	{
		if(enchant.containsKey(player))
			enchant.remove(player);
		
		enchant.put(player, item);
	}*/
	 public void giveItemsToPlayer(List<ItemStack> itemStackList, Player player, TeamColor teamColor) {
	        for (ItemStack itemStack : itemStackList) {
	        		        
	        	api = BedwarsAPI.getInstance();
	        	
	        	ColorChanger colorChanger = api.getColorChanger();
	        	
	            final String materialName = itemStack.getType().toString();
	            final PlayerInventory playerInventory = player.getInventory();

	            if (materialName.contains("HELMET")) {
	                playerInventory.setHelmet(colorChanger.applyColor(teamColor, itemStack));
	            } else if (materialName.contains("CHESTPLATE")) {
	                playerInventory.setChestplate(colorChanger.applyColor(teamColor, itemStack));
	            } else if (materialName.contains("LEGGINGS")) {
	                playerInventory.setLeggings(colorChanger.applyColor(teamColor, itemStack));
	            } else if (materialName.contains("BOOTS")) {
	                playerInventory.setBoots(colorChanger.applyColor(teamColor, itemStack));
	            } else {
	                playerInventory.addItem(colorChanger.applyColor(teamColor, itemStack));
	            }
	        }
	    }
	 
	 @EventHandler(priority = EventPriority.NORMAL)
	    public void onClick(InventoryClickEvent event)
	    {
		 
		 api = BedwarsAPI.getInstance();
		 
		 if (!(event.getWhoClicked() instanceof Player)) return;

			Player player = (Player) event.getWhoClicked();
			
		 if(!api.isPlayerPlayingAnyGame(player)) return;
		 	
	        if(event.getSlotType() == SlotType.ARMOR)
	        {
	            event.setCancelled(true);
	        }
	    }
	 
	 @EventHandler
	 public void onItemDrop( PlayerDropItemEvent evt )
	 {
		 api = BedwarsAPI.getInstance();
	
		 if(!api.isPlayerPlayingAnyGame(evt.getPlayer())) return;
	
     
	 ArrayList<Material> allowed = new ArrayList<Material>();
	 allowed.add(Material.GOLD_INGOT);
	 allowed.add(Material.EMERALD);
	 allowed.add(Material.DIAMOND);
	 allowed.add(Material.IRON_INGOT);
	 allowed.add(Material.BLACK_WOOL);
	 allowed.add(Material.BROWN_WOOL);
	 allowed.add(Material.BLUE_WOOL);
	 allowed.add(Material.CYAN_WOOL);
	 allowed.add(Material.GRAY_WOOL);
	 allowed.add(Material.GREEN_WOOL);
	 allowed.add(Material.LIGHT_BLUE_WOOL);
	 allowed.add(Material.LIME_WOOL);
	 allowed.add(Material.WHITE_WOOL);
	 allowed.add(Material.RED_WOOL);
	 allowed.add(Material.LIGHT_GRAY_WOOL);
	 allowed.add(Material.YELLOW_WOOL);
	 if( !allowed.contains(evt.getItemDrop().getItemStack().getType()) )
	 {
		 evt.setCancelled(true);
		 evt.getPlayer().getInventory().remove(evt.getItemDrop().getItemStack());
	 }
	 }
	 
	
}
