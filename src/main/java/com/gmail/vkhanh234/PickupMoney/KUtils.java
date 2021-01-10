package com.gmail.vkhanh234.PickupMoney;

import org.bukkit.ChatColor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Created by Admin on 24/7/2015
 * Last revised, 10/1/2020
 *
 * @author Khanh
 * @author md5sha256
 */
public class KUtils {

    private static final Random SHARED_RANDOM = new Random();

    public static String addSpace(String s) {
        return s.replace("-", " ");
    }

    public static float getRandom(String level) {
        if (level.contains("-")) {
            String[] spl = level.split("-");
            return round(randomNumber(Integer.parseInt(spl[0]), Integer.parseInt(spl[1])), 2);
        } else return Integer.parseInt(level);
    }

    public static int getRandomInt(String level) {
        if (level.contains("-")) {
            String[] spl = level.split("-");
            return getRandomInt(Integer.parseInt(spl[0]), Integer.parseInt(spl[1]));
        } else return Integer.parseInt(level);
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public static float randomNumber(int min, int max) {
        return SHARED_RANDOM.nextFloat() * (max - min) + min;
    }

    public static int getRandomInt(int min, int max) {
        return SHARED_RANDOM.nextInt((max - min) + 1) + min;
    }

    public static boolean getSuccess(int percent) {
        int i = getRandomInt(1, 100);
        return i <= percent;
    }

    public static String backColor(String name) {
        return name.replace("§", "&");
    }

    public static String convertColor(String name) {
        return ChatColor.translateAlternateColorCodes('&', name);
    }
}
