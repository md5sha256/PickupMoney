package com.gmail.vkhanh234.PickupMoney.listener;

import com.gmail.vkhanh234.PickupMoney.KUtils;
import com.gmail.vkhanh234.PickupMoney.PickupMoney;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

public class MainListener implements Listener {

    private final PickupMoney plugin;

    public MainListener(PickupMoney plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getEntity();
        Item item = event.getItem();
        if (item.getCustomName() == null) {
            return;
        }
        String name = ChatColor.stripColor(item.getCustomName());
        //if(name!=null && ChatColor.stripColor(language.get("nameSyntax")).replace("{money}", "").equals(name.replaceAll(regex, ""))){
        event.setCancelled(true);
        String money = plugin.getMoney(name);
        if (player.hasPermission("PickupMoney.pickup")) {
            item.remove();
            float amount = Float.parseFloat(money);
            if (plugin.pickupMulti.containsKey(player.getUniqueId()))
                amount *= plugin.pickupMulti.get(player.getUniqueId());
            plugin.giveMoney(amount, player);
            player.sendMessage(plugin.language.get("pickup").replace("{money}", String.valueOf(amount)));
            if (PickupMoney.fc.getBoolean("sound.enable")) {
                player.getLocation().getWorld().playSound(player.getLocation(), Sound.valueOf(PickupMoney.fc.getString("sound.type"))
                        , (float) PickupMoney.fc.getDouble("sound.volumn")
                        , (float) PickupMoney.fc.getDouble("sound.pitch"));
            }
//		}
        }
    }


    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!PickupMoney.fc.getBoolean("enableEntitiesDrop")) {
            return;
        }
        final LivingEntity entity = event.getEntity();
        final Player killer = entity.getKiller();
        if (killer == null || !plugin.checkWorld(entity.getLocation())) {
            return;
        }
        final String name = entity.getType().toString();
        if (!(plugin.entities.contain(name) && plugin.entities.getEnable(name) && KUtils.getSuccess(plugin.entities.getChance(name)))) {
            return;
        }
        if (entity instanceof Player) {
            Player p = (Player) entity;
            for (int i = 0; i < KUtils.getRandomInt(plugin.entities.getAmount(name)); i++) {
                float money = plugin.getMoneyOfPlayer((Player) entity, plugin.entities.getMoney(name));
                if (plugin.entities.getCost(name)) {
                    plugin.costMoney(money, p);
                    p.sendMessage(plugin.language.get("dropOut").replace("{money}", String.valueOf(money)));
                }
                plugin.spawnMoney(killer, money, entity.getLocation());
            }
        } else {
            int perc = 100;
            if (plugin.spawners.contains(entity.getUniqueId())) perc = PickupMoney.fc.getInt("spawnerPercent");
            for (int i = 0; i < KUtils.getRandomInt(plugin.entities.getAmount(name)); i++) {
                plugin.spawnMoney(killer, KUtils.getRandom(plugin.entities.getMoney(name)) * perc / 100, entity.getLocation());
            }
        }
        plugin.spawnParticle(entity.getLocation());

    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (!PickupMoney.fc.getBoolean("enableBlocksDrop")) {
            return;
        }
        Block block = e.getBlock();
        if (!plugin.checkWorld(block.getLocation())) return;
        String name = block.getType().toString();
        if (plugin.blocks.contain(name) && plugin.blocks.getEnable(name) && KUtils.getSuccess(plugin.blocks.getChance(name))) {
            for (int i = 0; i < KUtils.getRandomInt(plugin.blocks.getAmount(name)); i++) {
                plugin.spawnMoney(e.getPlayer(), KUtils.getRandom(plugin.blocks.getMoney(name)), block.getLocation());
            }
            plugin.spawnParticle(block.getLocation());
        }
    }

    @EventHandler
    public void onSpawner(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            plugin.spawners.add(event.getEntity().getUniqueId());
        }
    }

    @EventHandler
    public void onHopper(InventoryPickupItemEvent event) {
        if (event.getInventory().getType().toString().equalsIgnoreCase("hopper") && event.getItem().getCustomName() != null) {
            event.setCancelled(true);
        }
    }
}
