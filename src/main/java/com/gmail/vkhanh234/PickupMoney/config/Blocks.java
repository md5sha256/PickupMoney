package com.gmail.vkhanh234.PickupMoney.config;

import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Admin on 22/8/2015
 * Last revised, 10/1/2020
 *
 * @author Khanh
 * @author md5sha256
 */
public class Blocks {
    private final PickupMoney plugin;
    private final Map<String, BlockData> map = new HashMap<>();
    private final FileConfiguration config;
    private final File configFile = new File("plugins/PickupMoney/blocks.yml");

    public Blocks(PickupMoney plugin) {
        this.plugin = plugin;
        config = YamlConfiguration.loadConfiguration(configFile);
        try {
            update();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        load();
    }

    public void update() throws IOException, InvalidConfigurationException {
        if (!configFile.exists()) {
            try (final InputStream inputStream = plugin.getResource("blocks.yml");
                 final InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(inputStream, "Failed to find blocks.yml!"))) {
                config.load(reader);
                config.save(configFile);
            }
        }
    }

    public void load() {
        for (String k : config.getKeys(false)) {
            BlockData e = new BlockData();
            e.enable = config.getBoolean(k + ".enable");
            e.chance = config.getInt(k + ".chance");
            e.money = config.getString(k + ".money");
            e.amount = config.getString(k + ".amount");
            map.put(k, e);
        }
    }

    public boolean contain(String name) {
        return map.containsKey(name);
    }

    public boolean getEnable(String name) {
        return map.get(name).enable;
    }

    public int getChance(String name) {
        return map.get(name).chance;
    }

    public String getMoney(String name) {
        return map.get(name).money;
    }

    public String getAmount(String name) {
        return map.get(name).amount;
    }

    private static class BlockData {
        private boolean enable;
        private int chance;
        private String money, amount;
    }
}
