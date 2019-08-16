package io.github.jroy.punish.command;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import io.github.jroy.punish.DatabaseManager;
import io.github.jroy.punish.gui.PunishGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishCommand implements CommandExecutor {

  private InventoryManager inventoryManager;
  private DatabaseManager databaseManager;

  public PunishCommand(InventoryManager inventoryManager, DatabaseManager databaseManager) {
    this.inventoryManager = inventoryManager;
    this.databaseManager = databaseManager;
    this.inventoryManager.init();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player) {
      Player player = (Player) sender;

      if (!player.hasPermission("trevor.mod")) {
        player.sendMessage(ChatColor.AQUA + "Punish>> " + ChatColor.YELLOW + "Insufficient Permissions!");
        return true;
      }

      if (args.length < 2) {
        player.sendMessage(ChatColor.AQUA + "Punish>> " + ChatColor.YELLOW + "Correct Usage: /punish <username> <reason>");
        return true;
      }
      //noinspection deprecation
      OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
      if (!target.hasPlayedBefore()) {
        player.sendMessage(ChatColor.AQUA + "Punish>> " + ChatColor.YELLOW + "This player has never joined the server!");
        return true;
      }

      StringBuilder reasonBuilder = new StringBuilder();
      for (String arg : args) {
        reasonBuilder.append(arg).append(" ");
      }
      String reason = reasonBuilder.toString().replaceFirst(args[0] + " ", "");

      SmartInventory.builder()
          .id("punishGui")
          .provider(new PunishGUI(databaseManager, target, reason, inventoryManager))
          .manager(inventoryManager)
          .title("Punish")
          .build().open(player);
      return true;
    }
    return false;
  }
}
