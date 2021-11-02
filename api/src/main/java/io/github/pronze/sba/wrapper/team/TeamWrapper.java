package io.github.pronze.sba.wrapper.team;

import io.github.pronze.sba.wrapper.game.GameWrapper;
import org.bukkit.ChatColor;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.lib.utils.BasicWrapper;

public class TeamWrapper extends BasicWrapper<Team> {

    public static TeamWrapper of(Team team) {
        return new TeamWrapper(team);
    }

    protected TeamWrapper(Team wrappedObject) {
        super(wrappedObject);
    }

    public GameWrapper getGame() {
        return GameWrapper.of(wrappedObject.getGame());
    }

    public String getName() {
        return wrappedObject.getName();
    }

    public ChatColor getChatColor() {
        return org.screamingsandals.bedwars.game.TeamColor.valueOf(wrappedObject.getColor().name()).chatColor;
    }
}
