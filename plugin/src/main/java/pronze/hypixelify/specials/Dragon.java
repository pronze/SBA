package pronze.hypixelify.specials;

import org.bukkit.entity.EnderDragon;
import pronze.hypixelify.SBAHypixelify;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.special.SpecialItem;
import pronze.hypixelify.config.SBAConfig;

@Getter
public class Dragon extends SpecialItem {
    private LivingEntity entity;
    private final Location location;
    private final Team team;
    private final Game game;

    public Dragon(Game game, Player player, Team team,
                  Location location) {
        super(game, player, team);
        this.game = game;
        this.player = player;
        this.team = team;
        this.location = location;
    }

    public void spawn() {
        final var color = TeamColor.fromApiColor(team.getColor());
        final var dragon = (org.bukkit.entity.EnderDragon) location.getWorld().spawnEntity(location, EntityType.ENDER_DRAGON);
        dragon.setPhase(EnderDragon.Phase.CIRCLING);
        dragon.setCustomName(SBAConfig.getInstance().getString("dragon.name-format")
                .replace("%teamcolor%", color.chatColor.toString())
                .replace("%team%", team.getName()));
        dragon.setCustomNameVisible(SBAConfig.getInstance().getBoolean("dragon.custom-name-enabled", true));

        try {
            dragon.setInvulnerable(false);
        } catch (Throwable ignored) {}

        entity = dragon;

        game.registerSpecialItem(this);
        Main.registerGameEntity(dragon, (org.screamingsandals.bedwars.game.Game) game);
    }
}
