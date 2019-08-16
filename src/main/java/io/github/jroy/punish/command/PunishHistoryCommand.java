package io.github.jroy.punish.command;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import io.github.jroy.punish.DatabaseManager;
import io.github.jroy.punish.gui.PunishHistoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishHistoryCommand implements CommandExecutor {

  private DatabaseManager databaseManager;
  private InventoryManager inventoryManager;

  public PunishHistoryCommand(DatabaseManager databaseManager, InventoryManager inventoryManager) {
    this.databaseManager = databaseManager;
    this.inventoryManager = inventoryManager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player) {
      Player player = (Player) sender;

      OfflinePlayer target = player;
      if (player.hasPermission("trevor.mod") && args.length >= 1) {
        //noinspection deprecation
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        if (!offlinePlayer.hasPlayedBefore()) {
          player.sendMessage(ChatColor.AQUA + "Punish>> " + ChatColor.YELLOW + "This player has never joined the server!");
          return true;
        }
        target = offlinePlayer;
      }

      SmartInventory.builder()
          .id("punishHistoryGui")
          .provider(new PunishHistoryGUI(databaseManager, target))
          .manager(inventoryManager)
          .title("History - " + target.getName())
          .build().open(player);
      return true;
    }
    return false;
  }
}
