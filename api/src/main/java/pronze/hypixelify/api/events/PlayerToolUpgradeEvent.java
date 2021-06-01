package pronze.hypixelify.api.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;

@RequiredArgsConstructor
@Getter
public class PlayerToolUpgradeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack stack;
    private final String name;
    private final RunningTeam team;
    private final Game game;
    private final ItemSpawnerType type;
    @Setter
    private String price = null;
    private boolean isCancelled = false;

    public ItemStack getStackFromPrice(int price){
        return type.getStack(price);
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return PlayerToolUpgradeEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PlayerToolUpgradeEvent.handlers;
    }
}
