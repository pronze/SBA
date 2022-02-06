package io.github.pronze.sba.data;

import lombok.Data;
import org.screamingsandals.bedwars.api.RunningTeam;

import java.util.HashMap;
import java.util.Map;

@Data
public final class GameData {
    private final Map<RunningTeam, GameTeamData> teamDataMap = new HashMap<>();

}
