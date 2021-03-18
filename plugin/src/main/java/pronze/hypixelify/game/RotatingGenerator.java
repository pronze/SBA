package pronze.hypixelify.game;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.ItemSpawner;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.Component;
import org.screamingsandals.bedwars.lib.ext.kyori.adventure.text.TextComponent;
import org.screamingsandals.bedwars.lib.hologram.Hologram;
import org.screamingsandals.bedwars.lib.hologram.HologramManager;
import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import org.screamingsandals.bedwars.lib.utils.AdventureHelper;
import org.screamingsandals.bedwars.lib.utils.visual.TextEntry;
import org.screamingsandals.bedwars.lib.world.LocationMapper;
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

        hologram = Hologram.of(LocationMapper.wrapLocation(spawner.getLocation().clone().add(0, holoHeight, 0)));
        arena.getGame().getConnectedPlayers().forEach(player -> hologram.addViewer(PlayerMapper.wrapPlayer(player)));
        final var holoText = SBAHypixelify
                .getConfigurator()
                .getStringList("floating-generator.holo-text");

        for (int i = 0; i < holoText.size(); i++) {
            hologram.addLine(i, TextEntry.of(holoText.get(i)));
        }
        hologram.show();

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
                if (time < 0) {
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
            hologram.setLine(i, TextEntry.of(lines.get(i)));
        }
        this.lines = new ArrayList<>(lines);
    }
}
