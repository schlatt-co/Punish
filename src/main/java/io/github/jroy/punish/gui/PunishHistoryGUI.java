package io.github.jroy.punish.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import io.github.jroy.punish.DatabaseManager;
import io.github.jroy.punish.model.BanToken;
import io.github.jroy.punish.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PunishHistoryGUI implements InventoryProvider {

  private DatabaseManager databaseManager;
  private OfflinePlayer target;

  public PunishHistoryGUI(DatabaseManager databaseManager, OfflinePlayer target) {
    this.databaseManager = databaseManager;
    this.target = target;
  }

  @Override
  public void init(Player player, InventoryContents contents) {
    ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
    SkullMeta headMeta = (SkullMeta) head.getItemMeta();
    //noinspection ConstantConditions
    headMeta.setOwningPlayer(target);
    headMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + target.getName());
    head.setItemMeta(headMeta);
    contents.set(0, 4, ClickableItem.empty(head));

    List<BanToken> history = databaseManager.getPunishHistory(target.getUniqueId(), 28);


    int row = 1;
    int column = 1;
    for (BanToken token : history) {
      if (column == 8) {
        column = 1;
        row++;
      }
      if (row == 5) {
        return;
      }

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
      lore.add("&fReason: &e" + token.getReason());
      if (token.getRemovedUuid() != null) {
        lore.add("");
        lore.add("&fRemoved by: &e" + Bukkit.getOfflinePlayer(token.getRemovedUuid()).getName());
        lore.add("&fRemoved Reason: &e" + token.getRemovedReason());
      } else if (token.getType().equals("ban") && ((System.currentTimeMillis() - token.getEpoch()) < token.getWait() || token.getWait() == 0L)) {
        shine = true;
      }


      contents.set(row, column, ClickableItem.empty(Util.item(material, "&a&l" + text, shine, lore)));
      column++;
    }
  }

  @Override
  public void update(Player player, InventoryContents contents) {

  }
}
