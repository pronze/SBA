package pronze.hypixelify.api.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.lib.sgui.utils.MapReader;

import java.util.Map;

@RequiredArgsConstructor
@Getter
public class ApplyPropertyToItemEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Game game;
    private final Player player;
    private final ItemStack stack;
    private final Map<String, Object> properties;
    private final MapReader reader;
    private String price = null;

    public static HandlerList getHandlerList() {
        return ApplyPropertyToItemEvent.handlers;
    }

    public void setPrice(String st) {
        price = st;
    }

    public MapReader getReader() {
        return reader;
    }

    /**
     * @return
     */
    public Game getGame() {
        return this.game;
    }

    /**
     * @return
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * @return
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * @return
     */
    public String getPropertyName() {
        return (String) this.properties.get("name");
    }

    /**
     * @param key
     * @return
     */
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    /**
     * @param key
     * @return
     */
    public String getStringProperty(String key) {
        return this.properties.get(key).toString();
    }

    /**
     * @param key
     * @return
     */
    public byte getByteProperty(String key) {
        return ((Number) this.properties.get(key)).byteValue();
    }

    /**
     * @param key
     * @return
     */
    public short getShortProperty(String key) {
        return ((Number) this.properties.get(key)).shortValue();
    }

    /**
     * @param key
     * @return
     */
    public int getIntProperty(String key) {
        return ((Number) this.properties.get(key)).intValue();
    }

    /**
     * @param key
     * @return
     */
    public long getLongProperty(String key) {
        return ((Number) this.properties.get(key)).longValue();
    }

    /**
     * @param key
     * @return
     */
    public float getFloatProperty(String key) {
        return ((Number) this.properties.get(key)).floatValue();
    }

    /**
     * @param key
     * @return
     */
    public double getDoubleProperty(String key) {
        return ((Number) this.properties.get(key)).doubleValue();
    }

    /**
     * @param key
     * @return
     */
    public boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(this.properties.get(key).toString());
    }

    /**
     * @param key
     * @return
     */
    public char getCharProperty(String key) {
        return (char) this.properties.get(key);
    }

    @Override
    public HandlerList getHandlers() {
        return ApplyPropertyToItemEvent.handlers;
    }

}