package io.github.jroy.punish.command;

import io.github.jroy.punish.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class AddCacheCommand implements CommandExecutor {

  private DatabaseManager databaseManager;

  public AddCacheCommand(DatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender.hasPermission("trevor.admin")) {
      String name = args[0];
      UUID uuid = UUID.fromString(args[1]);
      if (databaseManager.fetchCache(name) == null) {
        databaseManager.addCache(uuid, name);
      } else {
        databaseManager.updateCache(uuid, name);
      }
      sender.sendMessage("Added to cache.");
      return true;
    }
    sender.sendMessage("No Permission!");
    return true;
  }
}
