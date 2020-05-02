package io.github.jroy.punish.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import io.github.jroy.punish.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public class TimeSelectGUI implements InventoryProvider {
  private final SmartInventory inventory;
  private final Consumer<Long> onSelect;
  private int hours = 0;
  private int days = 0;
  private int weeks = 0;

  protected TimeSelectGUI(InventoryManager inventoryManager, String title, Consumer<Long> onSelect) {
    this.inventory =
        SmartInventory.builder()
            .id(title)
            .provider(this)
            .manager(inventoryManager)
            .size(5, 9)
            .title(title)
            .build();
    this.onSelect = onSelect;
  }

  private static ItemStack getRemoveItem(int quantity, String timePeriod) {
    ItemStack item = new ItemStack(Material.SNOWBALL, quantity);
    ItemMeta meta = item.getItemMeta();
    assert meta != null;
    meta.setDisplayName(ChatColor.GRAY + "Remove " + ChatColor.YELLOW + quantity + " " + timePeriod + "(s)");
    item.setItemMeta(meta);
    return item;
  }

  private static ItemStack getAddItem(int quantity, String timePeriod) {
    ItemStack item = new ItemStack(Material.SLIME_BALL, quantity);
    ItemMeta meta = item.getItemMeta();
    assert meta != null;
      meta.setDisplayName(ChatColor.GRAY +"Add " + ChatColor.YELLOW + quantity + " " + timePeriod + "(s)");
    item.setItemMeta(meta);
    return item;
  }

  private static ItemStack getClock(int hours, int days, int weeks) {
    return Util.item(Material.CLOCK, ChatColor.YELLOW + "Punish Time: ",
        "&7" + hours + " hours", "&7" + days + " days", "&7" + weeks + " weeks"
    );
  }

  private void updateClock(InventoryContents contents) {
    contents.set(0, 4, ClickableItem.empty(getClock(hours, days, weeks)));
  }

  private void changeHours(int delta, InventoryContents contents) {
    hours = Math.min(Math.max(0, hours + delta), 64);
    ItemStack stack = Util.item(Material.OAK_BUTTON, "&eHours");
    stack.setAmount(hours);
    contents.set(1, 4, ClickableItem.empty(stack));
    updateClock(contents);
  }

  private void changeDays(int delta, InventoryContents contents) {
    days = Math.min(Math.max(0, days + delta), 64);
    ItemStack stack = Util.item(Material.STICK, "&cDays");
    stack.setAmount(days);
    contents.set(2, 4, ClickableItem.empty(stack));
    updateClock(contents);
  }

  private void changeWeeks(int delta, InventoryContents contents) {
    weeks = Math.min(Math.max(0, weeks + delta), 64);
    ItemStack stack = Util.item(Material.OAK_PLANKS, "&4Weeks");
    stack.setAmount(weeks);
    contents.set(3, 4, ClickableItem.empty(stack));
    updateClock(contents);
  }

  private long getTime() {
    return (hours * 3600000L) + (days * 86400000L) + (weeks * 604800000);
  }

  public void show(Player player) {
    inventory.open(player);
  }

  @Override
  public void init(Player player, InventoryContents contents) {
    // B = Border, - = Subtract, + = Add, HDW = [Hours, Days, Weeks], C = Clock
    // B B B B C B B B B
    // B - - - H + + + B
    // B - - - D + + + B
    // B - - - W + + + B
    // B B B y B n B B B
    contents.fillBorders(ClickableItem.empty(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)));
    updateClock(contents);
    changeHours(0, contents);
    changeDays(0, contents);
    changeWeeks(0, contents);

    //Hour selectors
    contents.set(1, 1, ClickableItem.of(getRemoveItem(12, "hour"), (c) -> changeHours(-12, contents)));
    contents.set(1, 2, ClickableItem.of(getRemoveItem(6, "hour"), (c) -> changeHours(-6, contents)));
    contents.set(1, 3, ClickableItem.of(getRemoveItem(1, "hour"), (c) -> changeHours(-1, contents)));

    contents.set(1, 5, ClickableItem.of(getAddItem(1, "hour"), (c) -> changeHours(1, contents)));
    contents.set(1, 6, ClickableItem.of(getAddItem(6, "hour"), (c) -> changeHours(6, contents)));
    contents.set(1, 7, ClickableItem.of(getAddItem(12, "hour"), (c) -> changeHours(12, contents)));

    //Day selectors
    contents.set(2, 1, ClickableItem.of(getRemoveItem(12, "day"), (c) -> changeDays(-12, contents)));
    contents.set(2, 2, ClickableItem.of(getRemoveItem(6, "day"), (c) -> changeDays(-6, contents)));
    contents.set(2, 3, ClickableItem.of(getRemoveItem(1, "day"), (c) -> changeDays(-1, contents)));

    contents.set(2, 5, ClickableItem.of(getAddItem(1, "day"), (c) -> changeDays(1, contents)));
    contents.set(2, 6, ClickableItem.of(getAddItem(6, "day"), (c) -> changeDays(6, contents)));
    contents.set(2, 7, ClickableItem.of(getAddItem(12, "day"), (c) -> changeDays(12, contents)));

    //Week selectors
    contents.set(3, 1, ClickableItem.of(getRemoveItem(12, "week"), (c) -> changeWeeks(-12, contents)));
    contents.set(3, 2, ClickableItem.of(getRemoveItem(6, "week"), (c) -> changeWeeks(-6, contents)));
    contents.set(3, 3, ClickableItem.of(getRemoveItem(1, "week"), (c) -> changeWeeks(-1, contents)));

    contents.set(3, 5, ClickableItem.of(getAddItem(1, "week"), (c) -> changeWeeks(1, contents)));
    contents.set(3, 6, ClickableItem.of(getAddItem(6, "week"), (c) -> changeWeeks(6, contents)));
    contents.set(3, 7, ClickableItem.of(getAddItem(12, "week"), (c) -> changeWeeks(12, contents)));

    contents.set(4, 3, ClickableItem.of(Util.item(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Close"), e -> inventory.close(player)));
    contents.set(4, 5, ClickableItem.of(Util.item(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Accept"), e -> {
      inventory.close(player);
      onSelect.accept(getTime());
    }));
  }

  @Override
  public void update(Player player, InventoryContents contents) {
  }
}