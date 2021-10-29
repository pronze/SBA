package io.github.pronze.sba.wrapper;

import io.github.pronze.sba.AddonAPI;
import io.github.pronze.sba.data.GameTeamData;
import io.github.pronze.sba.game.GameWrapper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.BasicWrapper;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.LocationMapper;

import java.util.List;
import java.util.stream.Collectors;

public class RunningTeamWrapper extends BasicWrapper<RunningTeam> {

    public static RunningTeamWrapper of(RunningTeam team) {
        return new RunningTeamWrapper(team);
    }

    protected RunningTeamWrapper(RunningTeam wrappedObject) {
        super(wrappedObject);
    }

    public int countConnectedPlayers() {
        return wrappedObject.countConnectedPlayers();
    }

    public List<SBAPlayerWrapper> getConnectedPlayers() {
        return wrappedObject.getConnectedPlayers()
                .stream()
                .map(SBAPlayerWrapper::of)
                .collect(Collectors.toList());
    }

    public boolean isPlayerInTeam(PlayerWrapper player) {
        return wrappedObject.isPlayerInTeam(player.as(Player.class));
    }

    public boolean isDead() {
        return wrappedObject.isDead();
    }

    public boolean isAlive() {
        return wrappedObject.isAlive();
    }

    public boolean isTargetBlockExists() {
        return wrappedObject.isTargetBlockExists();
    }

    public LocationHolder getTargetBlockLocation() {
        return LocationMapper.wrapLocation(wrappedObject.getTargetBlock());
    }

    public ChatColor getChatColor() {
        return org.screamingsandals.bedwars.game.TeamColor.valueOf(wrappedObject.getColor().name()).chatColor;
    }

    public String getName() {
        return wrappedObject.getName();
    }

    public GameWrapper getGame() {
        return GameWrapper.of(wrappedObject.getGame());
    }

    public GameTeamData getTeamData() {
        return AddonAPI.getInstance()
                .getGameStorage(getGame())
                .orElseThrow()
                .getTeamData(this)
                .orElseThrow();
    }

    public Integer getSharpnessLevel() {
        return getTeamData().getSharpness();
    }

    public Integer getProtectionLevel() {
        return getTeamData().getProtection();
    }

    public Integer getEfficiencyLevel() {
        return getTeamData().getEfficiency();
    }

    public void setSharpnessLevel(Integer level) {
        getTeamData().setSharpness(level);
    }

    public void setProtectionLevel(Integer level) {
        getTeamData().setProtection(level);
    }

    public void setEfficiencyLevel(Integer level) {
        getTeamData().setEfficiency(level);
    }
}
