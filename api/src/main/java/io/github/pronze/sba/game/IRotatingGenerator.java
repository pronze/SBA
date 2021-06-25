package io.github.pronze.sba.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface IRotatingGenerator {

    void update(List<String> newLines);

    void destroy();

    void setLocation(Location location);

    void spawn(List<Player> viewers);

    void addViewer(Player player);

    void removeViewer(Player player);

}
