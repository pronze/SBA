package pronze.hypixelify.api.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Getter
public class SBAPlayerWrapperRegisteredEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final PlayerWrapper player;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return SBAPlayerWrapperRegisteredEvent.handlerList;
    }
}
