package io.github.jroy.punish.util;

import io.github.jroy.punish.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Util {

  public static ItemStack item(Material material, String name, String... lore) {
    return item(material, name, false, Arrays.asList(lore));
  }

  public static ItemStack item(Material material, String name, boolean shine, List<String> lore) {
    ItemStack itemStack = new ItemStack(material);
    ItemMeta itemMeta = itemStack.getItemMeta();
    //noinspection ConstantConditions
    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
    List<String> lores = new ArrayList<>();
    for (String curLore : lore) {
      lores.add(ChatColor.translateAlternateColorCodes('&', curLore));
    }
    itemMeta.setLore(lores);
    if (shine) {
      itemMeta.addEnchant(DatabaseManager.glowEnchantment, 1, true);
    }
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static String convertString(long time) {
    if (time == -1) {
      return "Permanent";
    }

    TimeUnit type;

    if (time < 60000) {
      type = TimeUnit.SECONDS;
    } else if (time < 3600000) {
      type = TimeUnit.MINUTES;
    } else if (time < 86400000) {
      type = TimeUnit.HOURS;
    } else {
      type = TimeUnit.DAYS;
    }


    String text;
    double num;
    if (type == TimeUnit.DAYS) {
      text = (num = trim(time / 86400000d)) + " Day";
    } else if (type == TimeUnit.HOURS) {
      text = (num = trim(time / 3600000d)) + " Hour";
    } else if (type == TimeUnit.MINUTES) {
      text = (num = trim(time / 60000d)) + " Minute";
    } else {
      text = (num = trim(time / 1000d)) + " Second";
    }

    if (num != 1)
      text += "s";

    return text;
  }

  private static double trim(double d) {
    DecimalFormat twoDForm = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
    return Double.parseDouble(twoDForm.format(d));
  }
}
