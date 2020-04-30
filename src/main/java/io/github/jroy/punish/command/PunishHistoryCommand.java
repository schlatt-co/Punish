package io.github.jroy.punish.command;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import io.github.jroy.punish.DatabaseManager;
import io.github.jroy.punish.gui.PunishHistoryGUI;
import io.github.jroy.punish.gui.PunishUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PunishHistoryCommand implements CommandExecutor {

  private final DatabaseManager databaseManager;
  private final InventoryManager inventoryManager;

  public PunishHistoryCommand(DatabaseManager databaseManager, InventoryManager inventoryManager) {
    this.databaseManager = databaseManager;
    this.inventoryManager = inventoryManager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player) {
      Player player = (Player) sender;

      PunishUser target = new PunishUser(player.getName(), player.getUniqueId(), player);
      if (player.hasPermission("trevor.mod") && args.length >= 1) {
        //noinspection deprecation
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        target.setName(offlinePlayer.getName());
        target.setUuid(offlinePlayer.getUniqueId());
        target.setPlayer(offlinePlayer);

        if (!offlinePlayer.isOnline()) {
          UUID uuid = databaseManager.fetchCache(target.getName());
          if (uuid == null) {
            player.sendMessage(ChatColor.AQUA + "Punish>> " + ChatColor.YELLOW + "This player has never joined the server!");
            return true;
          }
          target.setUuid(uuid);
        }
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
