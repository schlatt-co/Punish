package io.github.jroy.punish;

import fr.minuskube.inv.InventoryManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class Punish extends JavaPlugin {

  @Override
  public void onEnable() {
    log("Loading Punish...");
    try {
      DatabaseManager databaseManager = new DatabaseManager(this);
      //noinspection ConstantConditions
      getCommand("punish").setExecutor(new PunishCommand(new InventoryManager(this), databaseManager));
      getServer().getPluginManager().registerEvents(databaseManager, this);
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
      getPluginLoader().disablePlugin(this);
    }
  }

  void log(String text) {
    System.out.println("[Punish] " + text);
  }
}
