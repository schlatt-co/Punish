package io.github.jroy.punish.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import io.github.jroy.punish.DatabaseManager;
import io.github.jroy.punish.model.HistoryToken;
import io.github.jroy.punish.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PunishHistoryGUI implements InventoryProvider {

  private final DatabaseManager databaseManager;
  private final PunishUser target;

  public PunishHistoryGUI(DatabaseManager databaseManager, PunishUser target) {
    this.databaseManager = databaseManager;
    this.target = target;
  }

  @Override
  public void init(Player player, InventoryContents contents) {
    ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
    SkullMeta headMeta = (SkullMeta) head.getItemMeta();
    //noinspection ConstantConditions
    headMeta.setOwningPlayer(target.getPlayer());
    headMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + target.getName());
    List<String> headLore = new ArrayList<>();
    OffsetDateTime joinDate = Instant.ofEpochMilli(target.getPlayer().getFirstPlayed()).atOffset(ZoneOffset.UTC);
    headLore.add(ChatColor.WHITE + "Join Date: " + joinDate.getMonth().getDisplayName(TextStyle.FULL, Locale.US) + " " + joinDate.getDayOfMonth() + " " + joinDate.getYear());
    headMeta.setLore(headLore);
    head.setItemMeta(headMeta);
    contents.set(0, 4, ClickableItem.empty(head));

    List<HistoryToken> history = Util.buildHistory(databaseManager, target.getUuid(), 28);
    int row = 1;
    int column = 1;
    for (HistoryToken token : history) {
      if (column == 8) {
        column = 1;
        row++;
      }
      if (row == 5) {
        return;
      }

      contents.set(row, column, ClickableItem.empty(token.getItem()));
      column++;
    }
  }

  @Override
  public void update(Player player, InventoryContents contents) {

  }
}
