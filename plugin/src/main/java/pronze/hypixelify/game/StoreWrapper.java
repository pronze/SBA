package pronze.hypixelify.game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.jitse.npclib.api.NPC;
import org.bukkit.entity.Entity;
import org.screamingsandals.bedwars.game.GameStore;

@AllArgsConstructor(staticName = "of")
@Getter
public class StoreWrapper {
    private final Entity entity;
    private final NPC npc;
    private final GameStore store;
    private final Type type;

    public enum Type {
        UPGRADES,
        NORMAL;

        public static Type of(String storeFile) {
            switch (storeFile) {
                case "shop.yml":
                    return NORMAL;
                case "upgradeShop.yml":
                    return UPGRADES;
            }
            return NORMAL;
        }
    }
}
