package io.github.pronze.sba.config;

import java.util.List;

/**
 * Represents a configurator implementation.
 */
public interface IConfigurator {
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
     * @param path path of the double to look up
     * @param def the default value to return if the path is not found
     * @return a double found from the config, def otherwise
     */
    double getDouble(String path, double def);
    /**
     *
     * @param path path of the String to look up
     * @param def the default value to return if the path is not found
     * @return a string found from the config, def otherwise
     */
    String getString(String path, String def);

    /**
     *
     * @param path path of the string list to lookup.
     * @return a list containing string objects
     */
    List<String> getStringList(String path);

    List<String> getSubKeys(String string);
    /**
     *
     * @param path path of the integer to lookup.
     * @param def the default value to return if the path is not found
     * @return an integer that was found, returns def otherwise.
     */
    Integer getInt(String path, Integer def);

    /**
     *
     * @param path path of the Byte to lookup.
     * @param def The default value to return if the path is not found
     * @return a byte that was found, returns def otherwise.
     */
    Byte getByte(String path, Byte def);

    /**
     *
     * @param path path of the boolean to lookup.
     * @param def the default value to return if the path is not found.
     * @return a boolean that was found, returns def otherwise.
     */
    Boolean getBoolean(String path, boolean def);

    /**
     * Saves the loaded configuration to sbaconfig.yml.
     */
    void saveConfig();
}

