package io.github.pronze.sba.wrapper.event;

import io.github.pronze.sba.AddonAPI;
import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.wrapper.RunningTeamWrapper;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import io.github.pronze.sba.wrapper.TeamWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerJoinedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import org.screamingsandals.bedwars.api.events.BedwarsTargetBlockDestroyedEvent;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

@Service
public class BedWarsEventWrapper implements Listener {

    @OnPostEnable
    public void onPostEnable() {
        AddonAPI.getInstance().registerListener(this);
    }

    @EventHandler
    public void onBedWarsPlayerJoinedEvent(BedwarsPlayerJoinedEvent event) {
        final var wrappedEvent = new BWPlayerJoinedEvent(
                GameWrapper.of(event.getGame()),
                SBAPlayerWrapper.of(event.getPlayer()),
                TeamWrapper.of(event.getTeam())
        );
        EventManager.fire(wrappedEvent);
    }

    @EventHandler
    public void onBedWarsPlayerLeaveEvent(BedwarsPlayerLeaveEvent event) {
        final var wrappedEvent = new BWPlayerLeaveEvent(
                GameWrapper.of(event.getGame()),
                SBAPlayerWrapper.of(event.getPlayer()),
                RunningTeamWrapper.of(event.getTeam())
        );
        EventManager.fire(wrappedEvent);
    }

    @EventHandler
    public void onBedWarsTargetBlockDestroyedEvent(BedwarsTargetBlockDestroyedEvent event) {
        final var wrappedEvent = new BWTargetBlockDestroyedEvent(
                GameWrapper.of(event.getGame()),
                event.getPlayer() == null ? null : SBAPlayerWrapper.of(event.getPlayer()),
                RunningTeamWrapper.of(event.getTeam())
        );
        EventManager.fire(wrappedEvent);
    }

    @EventHandler
    public void onBedWarsGameEndingEvent(BedwarsGameEndingEvent event) {
        final var wrappedEvent = new BWGameEndingEvent(
                GameWrapper.of(event.getGame()),
                RunningTeamWrapper.of(event.getWinningTeam())
        );
        EventManager.fire(wrappedEvent);
    }


}
