package io.github.pronze.sba.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;

@EqualsAndHashCode(callSuper=false)
@RequiredArgsConstructor
@Data
public class SBATeamUpgradePurchaseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Item originalItem;
    private final String name;
    private final RunningTeam team;
    private final Game game;
    private final ItemSpawnerType type;
    private String price = null;
    private boolean cancelled = false;

    public ItemStack getStackFromPrice(int price){
        return type.getStack(price);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return SBATeamUpgradePurchaseEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return SBATeamUpgradePurchaseEvent.handlers;
    }
}