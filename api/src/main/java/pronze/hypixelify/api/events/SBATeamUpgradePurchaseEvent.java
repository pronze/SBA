package pronze.hypixelify.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;

public class SBATeamUpgradePurchaseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private boolean isCancelled = false;
    private ItemStack stack;
    private String name;
    private  RunningTeam team;
    private Game game;
    private String price = null;
    private ItemSpawnerType type;

    public String getPrice(){
        return price;
    }

    public void setPrice(String pr){
        price = pr;
    }

    public ItemStack getStackFromPrice(int price){
        return type.getStack(price);
    }

    public SBATeamUpgradePurchaseEvent(Player player, ItemStack stack, String name, RunningTeam team, Game game, ItemSpawnerType type){
        this.player = player;
        this.stack = stack;
        this.name = name;
        this.team = team;
        this.game = game;
        this.type = type;
    }

    public Game getGame(){
        return game;
    }

    public RunningTeam getTeam(){
        return team;
    }


    public String getName(){
        return name;
    }

    public ItemStack getUpgradedItem(){
        return stack;
    }

    public Player getPlayer(){
        return player;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return SBATeamUpgradePurchaseEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return SBATeamUpgradePurchaseEvent.handlers;
    }
}