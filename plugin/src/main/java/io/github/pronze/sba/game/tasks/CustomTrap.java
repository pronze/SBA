package io.github.pronze.sba.game.tasks;

import java.util.List;

import org.bukkit.potion.PotionEffect;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CustomTrap {
    public String identifier;
    /*Possible values: enemy, team, all */
    public String target = "enemy";
    public List<PotionEffect> effects;
}
