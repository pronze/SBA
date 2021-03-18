package pronze.hypixelify.api.config;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ConfiguratorAPI {

    /**
     * Loads the default configuration of the plugin.
     */
    void loadDefaults();

    /**
     * Upgrades the following resource files: shop.yml, upgradeShop.yml, config.yml.
     */
    void upgrade();

    /**
     *
     * @param modify if true, the config will be overwritten to the default value.
     * @param path Path of the Object to look up
     * @param defValue The default value to return if the path is not found
     */
    void checkOrSetConfig(AtomicBoolean modify, String path, @NotNull Object defValue);

    /**
     *
     * @param path Path of the String to look up
     * @param def The default value to return if the path is not found
     * @return
     */
    String getString(String path, String def);

    /**
     *
     * @param path Path of the string list to lookup.
     * @return A {@link List} containing string objects
     */
    List<String> getStringList(String path);

    /**
     *
     * @param path Path of the integer to lookup.
     * @param def The default value to return if the path is not found
     * @return an integer that was found, returns def otherwise.
     */
    Integer getInt(String path, Integer def);

    /**
     *
     * @param path Path of the Byte to lookup.
     * @param def The default value to return if the path is not found
     * @return an byte that was found, returns def otherwise.
     */
    Byte getByte(String path, Byte def);

    /**
     *
     * @param path Path of the boolean to lookup.
     * @param def The default value to return if the path is not found.
     * @return an boolean that was found, returns def otherwise.
     */
    Boolean getBoolean(String path, boolean def);
}

