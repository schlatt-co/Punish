package io.github.jroy.punish.util;

import io.github.jroy.pluginlibrary.PluginLibrary;
import io.github.jroy.punish.DatabaseManager;
import io.github.jroy.punish.model.BanToken;
import io.github.jroy.punish.model.HistoryToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Util {

  public static List<HistoryToken> buildHistory(DatabaseManager databaseManager, UUID uuid, int limit) {
    List<HistoryToken> history = new ArrayList<>();
    List<BanToken> rawHistory = databaseManager.getPunishHistory(uuid, limit);
    if (rawHistory != null) {
      for (BanToken token : rawHistory) {
        Material material;
        String text;
        switch (token.getCategory()) {
          case "null": {
            material = Material.PAPER;
            text = "Warning";
            break;
          }
          case "perm": {
            material = Material.REDSTONE_BLOCK;
            text = "Permanent Ban";
            break;
          }
          case "chat": {
            material = Material.WRITABLE_BOOK;
            text = "Chat Offense";
            break;
          }
          case "general": {
            material = Material.HOPPER;
            text = "General Offense";
            break;
          }
          case "client": {
            material = Material.IRON_SWORD;
            text = "Client Mod";
            break;
          }
          default: {
            material = Material.FIREWORK_ROCKET;
            text = "Unknown";
            break;
          }
        }

        boolean shine = false;
        List<String> lore = new ArrayList<>();
        lore.add("&fSeverity: &e" + token.getSev());
        if (token.getType().equals("ban")) {
          lore.add("&fLength: &e" + Util.convertString(token.getWait()));
        }
        lore.add("&fDate: &e" + new Date(token.getEpoch()));
        lore.add("&fStaff: &e" + Bukkit.getOfflinePlayer(token.getStaffUuid()).getName());
        lore.add("");
        List<String> reasonList = Util.wrapLore(token.getReason());
        lore.add("&fReason: &e" + reasonList.get(0));
        reasonList.remove(0);
        for (String str : reasonList) {
          lore.add("&e" + str);
        }
        if (token.getRemovedUuid() != null) {
          lore.add("");
          lore.add("&fRemoved by: &e" + Bukkit.getOfflinePlayer(token.getRemovedUuid()).getName());
          List<String> removeReasonList = Util.wrapLore(token.getRemovedReason());
          lore.add("&fRemoved Reason: &e" + removeReasonList.get(0));
          removeReasonList.remove(0);
          for (String str : removeReasonList) {
            lore.add("&e" + str);
          }
        } else if (token.getType().equals("ban") && ((System.currentTimeMillis() - token.getEpoch()) < token.getWait() || token.getWait() == 0L)) {
          shine = true;
        }

        history.add(new HistoryToken(item(material, "&a&l" + text, shine, lore), token.getRemovedUuid() == null ? token.getId() : null));
      }
    }
    return history;
  }

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
      itemMeta.addEnchant(PluginLibrary.glowEnchantment, 1, true);
    }
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static String convertString(long time) {
    if (time <= 0) {
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

  public static List<String> wrapLore(String string) {
    StringBuilder sb = new StringBuilder(string);

    int i = 0;
    while (i + 35 < sb.length() && (i = sb.lastIndexOf(" ", i + 35)) != -1) {
      sb.replace(i, i + 1, "\n");
    }
    return new LinkedList<>(Arrays.asList(sb.toString().split("\n")));
  }
}
