package io.github.pronze.sba.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.simpleinventories.inventory.Price;

import io.github.pronze.sba.utils.Logger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Service
public class QuickBuyConfig {

    private JavaPlugin plugin;
    private File directory;

    public static QuickBuyConfig getInstance() {
        return ServiceManager.get(QuickBuyConfig.class);
    }

    public QuickBuyConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        directory = plugin.getDataFolder().toPath().resolve("quickbuy").toFile();
        load();
    }

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    public class UserQuickBuyConfig {
        private Map<String, QuickBuyItem> items = new HashMap<>();
        @NonNull
        private UUID userId;
        @Data
        @Accessors(fluent = true)
        @NoArgsConstructor
        @AllArgsConstructor
        public class QuickBuyItem {
            private Material material;
            private int amount;
            private String resource;

            public QuickBuyItem save() {
                UserQuickBuyConfig.this.save();
                return this;
            }
        }

        public UserQuickBuyConfig save() {
            QuickBuyConfig.this.save(this);
            return this;
        }

        public QuickBuyItem of(String id) {
            return items.get(id);
        }

        public UserQuickBuyConfig set(String id, QuickBuyItem val) {
            items.put(id, val);
            return this;
        }

        public UserQuickBuyConfig set(String quickBuyId, @NotNull Material type, Price price) {
            QuickBuyItem item = new QuickBuyItem(type, price.getAmount(), price.getCurrency());
            return set(quickBuyId, item);
        }
    }

    private Map<UUID, UserQuickBuyConfig> config = new HashMap<>();

    public UserQuickBuyConfig of(OfflinePlayer op) {
        if (!config.containsKey(op.getUniqueId()))
            config.put(op.getUniqueId(), new UserQuickBuyConfig(op.getUniqueId()));
        return config.get(op.getUniqueId());
    }

    public void load() {
        try {
            directory.mkdir();
        } catch (Throwable t) {
            Logger.error("Could not create quickbuy directory due to {}", t);
        }

        Arrays.stream(directory.listFiles()).forEach(file->{
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            var fileName = file.getName();
            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            UserQuickBuyConfig userConfig = new UserQuickBuyConfig(UUID.fromString(fileNameWithoutExtension));

            config.getKeys(false).forEach(key->{
                var section = config.getConfigurationSection(key);
                int amount = section.getInt("amount");
                String resource = section.getString("resource");
                String material = section.getString("material");
                Material mat = Material.valueOf(material);
                userConfig.set(key, mat, Price.of(amount,resource));
            });
            this.config.put(userConfig.userId(), userConfig);
        });
    }

    public void save() {
        try {
            directory.mkdir();
        } catch (Throwable t) {
            Logger.error("Could not create quickbuy directory due to {}", t);
        }
        config.values().forEach(this::save);
    }

    public void save(UserQuickBuyConfig userConfig) {
        File file = new File(directory + File.separator + userConfig.userId()+".yml");
        FileConfiguration config = new YamlConfiguration();
        userConfig.items().forEach((section,content)->{
            var sectionYaml = config.createSection(section);
            sectionYaml.set("material", content.material.toString());
            sectionYaml.set("amount", content.amount());
            sectionYaml.set("resource", content.resource());
        });
        try {
            config.save(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
