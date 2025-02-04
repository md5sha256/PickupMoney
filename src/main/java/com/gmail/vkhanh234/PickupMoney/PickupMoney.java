package com.gmail.vkhanh234.PickupMoney;

import com.darkblade12.particleeffect.ParticleEffect;
import com.gmail.vkhanh234.PickupMoney.config.Blocks;
import com.gmail.vkhanh234.PickupMoney.config.Entities;
import com.gmail.vkhanh234.PickupMoney.config.Language;
import com.gmail.vkhanh234.PickupMoney.listener.MainListener;
import com.gmail.vkhanh234.PickupMoney.listener.MultiplierListener;
import com.gmail.vkhanh234.PickupMoney.listener.MythicMobsListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PickupMoney extends JavaPlugin implements Listener {
    public static FileConfiguration fc;
    public static Economy economy = null;
    public Entities entities = new Entities(this);
    public Language language = new Language(this);
    public Map<UUID, Integer> dropMulti = new HashMap<>();
    public Map<UUID, Integer> pickupMulti = new HashMap<>();
    public Blocks blocks = new Blocks(this);
    public List<UUID> spawners = new ArrayList<>();
    public String regex = "[0-9]+\\.[0-9]+";
    String version = getDescription().getVersion();
    ConsoleCommandSender console = getServer().getConsoleSender();
    private final String prefix = "[PickupMoney] ";
    private final boolean preVer = false;

    {
        loadConfiguration();
        initConfig();
    }

    public static String getMessage(String type) {
        return KUtils.convertColor(fc.getString("Message." + type));
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    @Override
    public void onEnable() {
        if (fc.getBoolean("notiUpdate")) {
            sendConsole(ChatColor.GREEN + "Current version: " + ChatColor.AQUA + version);
            String vers = getNewestVersion();
            if (vers != null) {
                sendConsole(ChatColor.GREEN + "Latest version: " + ChatColor.RED + vers);
                if (!vers.equals(version)) {
                    sendConsole(ChatColor.RED + "There is a new version on Spigot!");
                    sendConsole(ChatColor.RED + "https://www.spigotmc.org/resources/11334/");
                }
            }
        }
        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            sendConsole("Vault is not installed or not enabled. ");
            sendConsole("This plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        String[] bukkver = getServer().getBukkitVersion().split("\\.");
        if (Integer.parseInt(bukkver[1].substring(0, 1)) < 8) {
            sendConsole("Server version is too old. Please update!");
            sendConsole("This plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            getLogger().info(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
        getServer().getPluginManager().registerEvents(new MultiplierListener(this), this);
        loadMultipliers();
        try {
            Class.forName("net.elseland.xikage.MythicMobs.API.Bukkit.Events.MythicMobDeathEvent");
            getServer().getPluginManager().registerEvents(new MythicMobsListener(this), this);
        } catch (ClassNotFoundException e) {
        }
    }

    private void loadMultipliers() {
        for (Player p : getServer().getOnlinePlayers()) {
            loadMultiplier(p);
        }
    }

    @Override
    public void onDisable() {
        // TODO Insert logic to be performed when the plugin is disabled
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("PickupMoney.command")) {
            sender.sendMessage(language.get("noPermission"));
            return true;
        }
        if (args.length >= 1) {
            try {
                if (args[0].equals("reload") && sender.hasPermission("PickupMoney.admincmd")) {
                    reloadConfig();
                    initConfig();
                    sender.sendMessage(language.get("reload"));
                } else if (args[0].equals("drop") && sender instanceof Player && args.length == 2) {
                    Player p = (Player) sender;
                    float money = KUtils.getRandom(args[1]);
                    if (money < fc.getInt("minimumCmdDrop")) {
                        p.sendMessage(language.get("miniumCmdDrop").replace("{money}", String.valueOf(fc.getInt("minimumCmdDrop"))));
                        return true;
                    }
                    Set<Material> set = null;
                    Block b = p.getTargetBlock(set, 6);
                    if (costMoney(money, p)) {
                        spawnMoney(p, money, b.getLocation());
                    } else {
                        p.sendMessage(language.get("noMoney"));
                    }
                } else showHelp(sender);
            } catch (Exception e) {
                showHelp(sender);
            }
        } else {
            showHelp(sender);
        }
        return true;
    }

    public void loadMultiplier(Player p) {
        int id = 1, ip = 1;
        for (PermissionAttachmentInfo perms : p.getEffectivePermissions()) {
            String perm = perms.getPermission();
            if (perm.toLowerCase().startsWith("pickupmoney.multiply.")) {
                String[] spl = perm.split("\\.");
                int num = Integer.parseInt(spl[3]);
                if (spl[2].equals("drop") && id < num) id = num;
                else if (spl[2].equals("pickup") && ip < num) ip = num;
            }
        }
        dropMulti.put(p.getUniqueId(), id);
        pickupMulti.put(p.getUniqueId(), ip);

    }

    public void sendConsole(String s) {
        console.sendMessage(prefix + s);
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "PickupMoney version " + version);
        if (sender.hasPermission("PickupMoney.admincmd"))
            sender.sendMessage(ChatColor.GREEN + "Reload - " + ChatColor.AQUA + "/pickupmoney reload");
        sender.sendMessage(ChatColor.GREEN + "Drop Money - " + ChatColor.AQUA + "/pickupmoney drop <amount>");
    }

    public float getMoneyOfPlayer(Player p, String val) {
        if (val.contains("%")) {
            String s = val.replace("%", "");
            int percent = KUtils.getRandomInt(s);
            return Double.valueOf(economy.getBalance(p)).floatValue() * percent / 100;
        } else return KUtils.getRandom(val);
    }

    public String getMoney(String name) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) return matcher.group(0);
        return "0";
    }

    public void giveMoney(float amount, Player p) {
        economy.depositPlayer(p, amount);
    }

    public boolean costMoney(float amount, Player p) {
        if (economy.getBalance(p) >= amount) {
            economy.withdrawPlayer(p, amount);
            return true;
        }
        return false;
    }

    public void spawnMoney(Player p, float money, Location l) {
        if (dropMulti.containsKey(p.getUniqueId())) money *= dropMulti.get(p.getUniqueId());
        Item item = l.getWorld().dropItemNaturally(l, getItem(Float.valueOf(money).intValue()));
        String m = String.valueOf(money);
        if (!m.contains(".")) {
            m = m + ".0";
        }
        item.setCustomName(language.get("nameSyntax").replace("{money}", m));
        item.setCustomNameVisible(true);
    }

    public void spawnParticle(Location l) {
        if (fc.getBoolean("particle.enable")) {
            final World world = l.getWorld();
            Objects.requireNonNull(world);
            final String legacyType = fc.getString("particle.type");
            final int amount = fc.getInt("particle.amount");
            try {
                assert legacyType != null;
                final Particle particle = Particle.valueOf(legacyType.toUpperCase(Locale.ENGLISH));
                world.spawnParticle(particle, l, amount);
            } catch (IllegalArgumentException ex) {

                ParticleEffect.fromName(legacyType).display((float) 0.5, (float) 0.5, (float) 0.5, 1, amount, l, 20);
            }
        }
    }

    public boolean checkWorld(Location location) {
        Objects.requireNonNull(location.getWorld(), "Location has no world!");
        final List<String> disabledWorlds = fc.getStringList("disableWorld");
        return !disabledWorlds.contains(location.getWorld().getName());
    }

    public ItemStack getItem(int money) {

        final String rawMaterial;
        if (money < fc.getInt("item.small.amount")) {
            rawMaterial = Objects.requireNonNull(fc.getString("item.small.type"), "item.small.type is null!");
        } else if (money < fc.getInt("item.normal.amount")) {
            rawMaterial = Objects.requireNonNull(fc.getString("item.normal.type"), "item.normal.type is null!");
        } else {
            rawMaterial = Objects.requireNonNull(fc.getString("item.big.type"), "item.small.type is null!");
        }
        final Material material = Material.valueOf(rawMaterial);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);
        assert meta != null;
        List<String> lore = new ArrayList<>();
        lore.add(String.valueOf(KUtils.getRandomInt(1, 100000000)));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void loadConfiguration() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        getConfig().options().copyDefaults(false);
    }

    private void initConfig() {
        fc = getConfig();
        language = new Language(this);
        entities = new Entities(this);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    private String getNewestVersion() {
        try {
            URL url = new URL("https://dl.dropboxusercontent.com/s/a890l19kn0fv32l/PickupMoney.txt");
            URLConnection con = url.openConnection();
            con.setConnectTimeout(2000);
            con.setReadTimeout(1000);
            InputStream in = con.getInputStream();
            return getStringFromInputStream(in);
        } catch (IOException ex) {
            sendConsole(ChatColor.RED + "Failed to check for update!");
        }
        return null;

    }
}
