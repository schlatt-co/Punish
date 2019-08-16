package io.github.jroy.punish;

import io.github.jroy.punish.model.BanToken;
import io.github.jroy.punish.util.GlowEnchantment;
import io.github.jroy.punish.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode")
public class DatabaseManager implements Listener {

  private Connection connection;

  public static GlowEnchantment glowEnchantment;

  static {
    try {
      Field acceptingNew = Enchantment.class.getDeclaredField("acceptingNew");
      acceptingNew.setAccessible(true);
      acceptingNew.set(null, true);
      glowEnchantment = new GlowEnchantment();
      EnchantmentWrapper.registerEnchantment(glowEnchantment);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  DatabaseManager(Punish plugin) throws ClassNotFoundException, SQLException {
    if (!plugin.getDataFolder().exists()) {
      //noinspection ResultOfMethodCallIgnored
      plugin.getDataFolder().mkdir();
    }

    Class.forName("org.sqlite.JDBC");
    connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/players.db");
    plugin.log("Connected to database!");
    connection.createStatement().execute("CREATE TABLE IF NOT EXISTS punishments( id integer PRIMARY KEY AUTOINCREMENT, targetUuid text NOT NULL, epoch integer NOT NULL, wait integer NOT NULL, reason text NOT NULL, type text NOT NULL, category text NOT NULL, sev text NOT NULL, staffUuid text NOT NULL, removedUuid text DEFAULT 'null' NOT NULL, removedReason text DEFAULT 'null');");
    plugin.log("Database tables initialized!");
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
    BanToken banToken = getActiveBan(event.getUniqueId());
    if (banToken == null || banToken.getRemovedUuid() != null) {
      return;
    }
    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "" + ChatColor.BOLD + "You have been banned for " + Util.convertString(banToken.getWait() == 0 ? -1 : banToken.getWait() - (System.currentTimeMillis() - banToken.getEpoch())) + " by " + Bukkit.getOfflinePlayer(banToken.getStaffUuid()).getName() + "\n" + ChatColor.WHITE + banToken.getReason() + "\n" + ChatColor.DARK_GREEN + "Appeal by doing in " + ChatColor.GREEN + "!ticket new appeal" + ChatColor.DARK_GREEN + " the " + ChatColor.GREEN + "#mc-support" + ChatColor.DARK_GREEN + " channel in discord");
  }

  private BanToken getActiveBan(UUID uuid) {
    try {
      PreparedStatement statement = connection.prepareStatement("SELECT * FROM punishments WHERE targetUuid = ? AND type = 'ban' AND (((" + System.currentTimeMillis() + " - epoch) < wait) OR wait = 0)");
      statement.setString(1, uuid.toString());
      ResultSet set = statement.executeQuery();
      if (set.next()) {
        return new BanToken(set.getInt("id"), UUID.fromString(set.getString("targetUuid")), set.getLong("epoch"), set.getLong("wait"), set.getString("reason"), set.getString("type"), set.getString("category"), set.getString("sev"), UUID.fromString(set.getString("staffUuid")), set.getString("removedUuid").equals("null") ? null : UUID.fromString(set.getString("removedUuid")), set.getString("removedReason"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<BanToken> getPunishHistory(UUID uuid, int limit) {
    try {
      PreparedStatement statement = connection.prepareStatement("SELECT * FROM punishments WHERE targetUuid = ? ORDER BY id DESC " + (limit != 0 ? "LIMIT " + limit : ""));
      statement.setString(1, uuid.toString());
      ResultSet set = statement.executeQuery();
      List<BanToken> list = new ArrayList<>();
      while (set.next()) {
        list.add(new BanToken(set.getInt("id"), UUID.fromString(set.getString("targetUuid")), set.getLong("epoch"), set.getLong("wait"), set.getString("reason"), set.getString("type"), set.getString("category"), set.getString("sev"), UUID.fromString(set.getString("staffUuid")), set.getString("removedUuid").equals("null") ? null : UUID.fromString(set.getString("removedUuid")), set.getString("removedReason")));
      }
      return list;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void addPunishment(OfflinePlayer target, long delay, String reason, String type, String category, String severity, Player staff) {
    try {
      PreparedStatement statement = connection.prepareStatement("INSERT INTO punishments(targetUuid, wait, reason, type, category, sev, staffUuid, epoch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
      statement.setString(1, target.getUniqueId().toString());
      statement.setLong(2, delay);
      statement.setString(3, reason);
      statement.setString(4, type);
      statement.setString(5, category);
      statement.setString(6, severity);
      statement.setString(7, staff.getUniqueId().toString());
      statement.setString(8, String.valueOf(System.currentTimeMillis()));
      statement.executeUpdate();

      Bukkit.broadcastMessage(ChatColor.AQUA + "Punish>> " + ChatColor.GRAY + staff.getName() + (type.equals("ban") ? " banned " + target.getName() + " for " + Util.convertString(delay) : " issued a friendly warning to " + target.getName()));

      if (type.equals("ban") && target.isOnline()) {
        Objects.requireNonNull(target.getPlayer()).kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "You have been banned for " + Util.convertString(delay) + " by " + staff.getName() + "\n" + ChatColor.WHITE + reason + "\n" + ChatColor.DARK_GREEN + "Appeal by doing in " + ChatColor.GREEN + "!ticket new appeal" + ChatColor.DARK_GREEN + " the " + ChatColor.GREEN + "#mc-support" + ChatColor.DARK_GREEN + " channel in discord");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void removePunishment(int id, UUID removedUuid, String removedReason) {
    try {
      PreparedStatement statement = connection.prepareStatement("UPDATE punishments SET removedUuid = ?, removedReason = ? WHERE id = ?");
      statement.setString(1, removedUuid.toString());
      statement.setString(2, removedReason);
      statement.setInt(3, id);
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
