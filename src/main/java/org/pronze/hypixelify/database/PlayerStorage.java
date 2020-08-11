package org.pronze.hypixelify.database;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.api.game.Game;
import java.util.HashMap;
import java.util.List;

public class PlayerStorage {
    private final Game game;
    private final HashMap<Player, List<ItemStack>> PlayerItems = new HashMap<>();


    public List<ItemStack> getItemsOfPlayer(Player player){
        return PlayerItems.get(player);
    }

    public void putPlayerItems(Player player, List<ItemStack> stacks){
        PlayerItems.put(player, stacks);
    }

    public PlayerStorage(Game game){
        this.game = game;
    }


    public Game getGame(){
        return game;
    }

}
