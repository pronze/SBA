package pronze.hypixelify.game;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.ItemSpawner;
import org.screamingsandals.bedwars.lib.nms.holograms.Hologram;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.game.Arena;

import java.util.ArrayList;
import java.util.List;

import static pronze.hypixelify.lib.lang.I.i18n;

@Data
public class RotatingGenerator {
    private final Arena arena;
    private ItemSpawner itemSpawner;
    private ItemStack itemStack;
    private int tierLevel;
    private int time;
    private Hologram hologram;
    private double holoHeight;
    private List<String> lines;

    public RotatingGenerator(Arena arena,
                             ItemSpawner spawner,
                             ItemStack itemStack) {
        this.arena = arena;
        this.lines = new ArrayList<>();
        this.itemSpawner = spawner;
        this.itemStack = itemStack;
        this.holoHeight = SBAHypixelify
                .getConfigurator()
                .config
                .getDouble("floating-generator.holo-height", 2.0);
        hologram = Main.getHologramManager()
                .spawnHologram(
                        arena.getGame().getConnectedPlayers(),
                        spawner.getLocation().clone().add(0, holoHeight, 0),
                        SBAHypixelify
                                .getConfigurator()
                                .getStringList("floating-generator.holo-text").toArray(new String[0])
                );

        new BukkitRunnable() {
            @Override
            public void run() {
                if (arena.getGame().getStatus() != GameStatus.RUNNING) {
                    this.cancel();
                    hologram.destroy();
                }

                final var newLines = new ArrayList<String>();
                final var matName = spawner.getItemSpawnerType().getMaterial() ==
                        Material.EMERALD ? "§a" + i18n("emerald") :
                        "§b" + i18n("diamond");
                for (var line : SBAHypixelify
                        .getConfigurator()
                        .getStringList("floating-generator.holo-text")) {
                    if (line == null) {
                        continue;
                    }
                    newLines.add(line
                            .replace("{tier}", String.valueOf(tierLevel))
                            .replace("{material}", matName + "§6")
                            .replace("{seconds}", String.valueOf(time)));
                }
                update(newLines);
                if (time <= 0) {
                    time = spawner.getItemSpawnerType().getInterval();
                }
            }
        }.runTaskTimer(SBAHypixelify.getInstance(), 0L, 20L);
    }

    public void update(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        if (lines.equals(getLines())) {
            return;
        }
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i) == null) {
                continue;
            }
            hologram.setLine(i, lines.get(i));
        }
        this.lines = new ArrayList<>(lines);
    }
}
