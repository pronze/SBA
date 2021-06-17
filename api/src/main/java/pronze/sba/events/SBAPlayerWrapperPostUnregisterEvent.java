package pronze.sba.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class SBAPlayerWrapperPostUnregisterEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final Player player;
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return SBAPlayerWrapperPostUnregisterEvent.handlerList;
    }
}
