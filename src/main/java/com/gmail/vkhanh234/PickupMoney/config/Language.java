package com.gmail.vkhanh234.PickupMoney.config;

import com.gmail.vkhanh234.PickupMoney.KUtils;
import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Created by Admin on 22/8/2015
 * Last revised, 10/1/2020
 *
 * @author Khanh
 * @author md5sha256
 */
public class Language {
    private final PickupMoney plugin;
    private final FileConfiguration config;
    private final File configFile = new File("plugins/PickupMoney/language.yml");

    public Language(PickupMoney plugin) {
        this.plugin = plugin;
        config = YamlConfiguration.loadConfiguration(configFile);
        try {
            update();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void update() throws IOException, InvalidConfigurationException {
        try (final InputStream inputStream = plugin.getResource("language.yml");
             final InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(inputStream, "Failed to find language.yml"))) {
            if (!configFile.exists()) {
                config.load(reader);
            } else {
                FileConfiguration c = YamlConfiguration.loadConfiguration(reader);
                for (String k : c.getKeys(true)) {
                    if (!config.contains(k)) {
                        config.set(k, c.get(k));
                    }
                }
            }
            config.save(configFile);
        }
    }

    public String get(String name) {
        return KUtils.convertColor(config.getString(name));
    }
}
