package io.github.jroy.punish.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
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

public class PunishGUI implements InventoryProvider {

  private final DatabaseManager databaseManager;
  private final PunishUser target;
  private final String reason;
  private final InventoryManager inventoryManager;

  public PunishGUI(DatabaseManager databaseManager, PunishUser target, String reason, InventoryManager inventoryManager) {
    this.databaseManager = databaseManager;
    this.target = target;
    this.reason = reason;
    this.inventoryManager = inventoryManager;
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public void init(Player player, InventoryContents contents) {
    ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
    SkullMeta headMeta = (SkullMeta) head.getItemMeta();
    //noinspection ConstantConditions
    headMeta.setOwningPlayer(target.getPlayer());
    headMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + target.getName());
    List<String> headLore = new ArrayList<>();
    for (String lore : Util.wrapLore("Reason: " + reason)) {
      headLore.add(ChatColor.WHITE + lore);
    }
    headLore.add("");
    OffsetDateTime joinDate = Instant.ofEpochMilli(target.getPlayer().getFirstPlayed()).atOffset(ZoneOffset.UTC);
    headLore.add(ChatColor.WHITE + "Join Date: " + joinDate.getMonth().getDisplayName(TextStyle.FULL, Locale.US) + " " + joinDate.getDayOfMonth() + " " + joinDate.getYear());
    headMeta.setLore(headLore);
    head.setItemMeta(headMeta);

    //Labels
    contents.set(0, 4, ClickableItem.empty(head));
    contents.set(1, 1, ClickableItem.of(
        Util.item(Material.WRITABLE_BOOK, "&a&lChat Offense", "&7Verbal Abuse, Spam, Harrassment, TOS, etc", "", "", "&7Click to set custom a ban length", "&7Please use time below if possible!"),
        c -> new TimeSelectGUI(inventoryManager, "Choose ban length",
            length -> databaseManager.addPunishment(target, length, reason, "ban", "chat", "time", player)).show(player)
    ));

    contents.set(1, 3, ClickableItem.of(Util.item(Material.HOPPER, "&a&lGeneral Offense", "&7Zero-tick machines, stealing, grief, etc", "", "", "&7Click to set custom a ban length", "&7Please use time below if possible!"),
        c -> new TimeSelectGUI(inventoryManager, "Choose ban length",
            length -> databaseManager.addPunishment(target, length, reason, "ban", "general", "time", player)).show(player)
    ));


    contents.set(1, 5, ClickableItem.of(Util.item(Material.IRON_SWORD, "&a&lClient Mod", "&7X-ray, Speed, Fly, Inventory Hacks, etc", "", "", "&7Click to set custom a ban length", "&7Please use time below if possible!"),
        c -> new TimeSelectGUI(inventoryManager, "Choose ban length",
            length -> databaseManager.addPunishment(target, length, reason, "ban", "client", "time", player)).show(player)
    ));

    //Sidebar
    contents.set(2, 7, ClickableItem.of(Util.item(Material.PAPER, "&a&lWarning", "", "&7Example Warning Input;", "&f   Spam - Constantly spamming advertising", "&f   TOS - Calling players 'cunt' and saying 'ni**er'", "&f   Killing - Killed Obama", "&f   0-Tick Farm - Made illegal redstone farm unintentionally"), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 0L, reason, "warning", "null", "1", player);
      inventoryManager.getInventory(player).get().close(player);
    }));
    contents.set(3, 7, ClickableItem.of(Util.item(Material.REDSTONE_BLOCK, "&a&lPermanent Ban", "&fBan Duration: &ePermanent", "", "&2Must supply detailed reason for Ban."), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 0L, reason, "ban", "perm", "4", player);
      inventoryManager.getInventory(player).get().close(player);
    }));

    //Chat Offenses
    contents.set(2, 1, ClickableItem.of(Util.item(Material.GREEN_DYE, "&a&lSeverity 1", "&fBan Duration: &e12 Hours", "&7Light Spam", "&f   Sending the same message 2-5 times", "&f    Flooding chat with adverts", "", "&2Give warning if 0 past offences & warnings."), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 43200000L, reason, "ban", "chat", "1", player);
      inventoryManager.getInventory(player).get().close(player);
    }));
    contents.set(3, 1, ClickableItem.of(Util.item(Material.YELLOW_DYE, "&a&lSeverity 2", "&fBan Duration: &e2 Days", "&7Medium Spam or TOS Violation", "    &fSending the same message 6-20 times", "    &f'Shut up cunt' or 'Stupid ni**er!!!1'", "", "&2Give warning if 0 past offences & warnings."), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 172800000L, reason, "ban", "chat", "2", player);
      inventoryManager.getInventory(player).get().close(player);
    }));
    contents.set(4, 1, ClickableItem.of(Util.item(Material.RED_DYE, "&a&lSeverity 3", "&fBan Duration: &e4 Days", "&7Severe Spam or TOS Violations", "    &fFlooding chat with the same message or with TOS violations", "    &fRepeatedly violating TOS for shock value"), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 345600000L, reason, "ban", "chat", "3", player);
      inventoryManager.getInventory(player).get().close(player);
    }));

    //General Offenses
    contents.set(2, 3, ClickableItem.of(Util.item(Material.GREEN_DYE, "&a&lSeverity 1", "&fBan Duration: &e3 Days", "&7First time light offenses", "    &fBuilding a zero-tick machine after warning", "    &fStealing a few diamond blocks", "    &fSmall Grief"), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 259200000L, reason, "ban", "general", "1", player);
      inventoryManager.getInventory(player).get().close(player);
    }));
    contents.set(3, 3, ClickableItem.of(Util.item(Material.YELLOW_DYE, "&a&lSeverity 2", "&fBan Duration: &e1 Week", "&7Medium Offenses", "    &fBuilding a banned farm after a severity 1 ban", "    &fStealing after a severity 1 ban or a more larger number of items", "    &fMedium sized grief"), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 604800000L, reason, "ban", "general", "2", player);
      inventoryManager.getInventory(player).get().close(player);
    }));
    contents.set(4, 3, ClickableItem.of(Util.item(Material.RED_DYE, "&a&lSeverity 3", "&fBan Duration &e30 Days", "&7Severe Offenses", "    &fBuilding a banned farm after a severity 2 ban", "    &fStealing either a huge amount or after a severity 2 ban", "    &fLarge grief or after a severity 2 ban"), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 2592000000L, reason, "ban", "general", "3", player);
      inventoryManager.getInventory(player).get().close(player);
    }));

    //Client Mod
    contents.set(2, 5, ClickableItem.of(Util.item(Material.GREEN_DYE, "&a&lSeverity 1", "&fBan Duration: &e4 Days", "&7First Time/Light Offense", "    &fX-Ray", "    &fSpeed", "    &fFly"), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 345600000L, reason, "ban", "client", "1", player);
      inventoryManager.getInventory(player).get().close(player);
    }));
    contents.set(3, 5, ClickableItem.of(Util.item(Material.YELLOW_DYE, "&a&lSeverity 2", "&fBan Duration: &e1.5 Weeks", "&7Second Time/Medium Offense", "    &fChest ESP", "    &fLight Nuker", "    &fX-Ray/Speed/Fly after a severity 1 ban"), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 907200000L, reason, "ban", "client", "2", player);
      inventoryManager.getInventory(player).get().close(player);
    }));
    contents.set(4, 5, ClickableItem.of(Util.item(Material.RED_DYE, "&a&lSeverity 3", "&fBan Duration: &e2 Weeks", "&7Third Time/Severe Offense", "    &fSevere Nuker", "    &fX-Ray/Speed/Fly after a severity 2 ban"), inventoryClickEvent -> {
      databaseManager.addPunishment(target, 1209600000L, reason, "ban", "client", "3", player);
      inventoryManager.getInventory(player).get().close(player);
    }));


    //History

    List<HistoryToken> history = Util.buildHistory(databaseManager, target.getUuid(), 9);
    int column = 0;
    for (HistoryToken historyToken : history) {
      contents.set(5, column, ClickableItem.of(historyToken.getItem(), inventoryClickEvent -> {
        if (historyToken.getId() != null && inventoryClickEvent.isRightClick()) {
          databaseManager.removePunishment(historyToken, player, reason);
          inventoryManager.getInventory(player).get().close(player);
        }
      }));
      column++;
    }
  }

  @Override
  public void update(Player player, InventoryContents contents) {
  }
}
