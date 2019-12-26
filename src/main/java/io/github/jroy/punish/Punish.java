package io.github.jroy.punish;

import fr.minuskube.inv.InventoryManager;
import io.github.jroy.punish.command.AddCacheCommand;
import io.github.jroy.punish.command.PunishCommand;
import io.github.jroy.punish.command.PunishHistoryCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class Punish extends JavaPlugin {

  @SuppressWarnings("ConstantConditions")
  @Override
  public void onEnable() {
    log("Loading Punish...");
    try {
      DatabaseManager databaseManager = new DatabaseManager(this);
      InventoryManager inventoryManager = new InventoryManager(this);
      inventoryManager.init();
      getCommand("punish").setExecutor(new PunishCommand(inventoryManager, databaseManager));
      getCommand("punishhistory").setExecutor(new PunishHistoryCommand(databaseManager, inventoryManager));
      getCommand("addcache").setExecutor(new AddCacheCommand(databaseManager));
      getServer().getPluginManager().registerEvents(databaseManager, this);
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
      getPluginLoader().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    if (DatabaseManager.webhookClient != null) {
      DatabaseManager.webhookClient.close();
    }
  }

  void log(String text) {
    System.out.println("[Punish] " + text);
  }
}
