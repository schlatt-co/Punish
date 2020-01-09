package io.github.jroy.punish;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import io.github.jroy.punish.gui.PunishUser;
import io.github.jroy.punish.model.BanToken;
import io.github.jroy.punish.model.NotificationToken;
import io.github.jroy.punish.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.*;
import java.util.*;

@SuppressWarnings("DuplicatedCode")
public class DatabaseManager implements Listener {

  private Punish plugin;
  private Connection connection;

  static WebhookClient webhookClient;
  private Map<UUID, NotificationToken> warningNotifications = new HashMap<>();

  DatabaseManager(Punish plugin) throws ClassNotFoundException, SQLException {
    this.plugin = plugin;
    plugin.getConfig().addDefault("webhookUrl", "url");
    plugin.getConfig().options().copyDefaults(true);
    plugin.saveConfig();
    plugin.log("Loaded config!");
    plugin.log("Connecting to webhook...");
    if (!Objects.equals(plugin.getConfig().getString("webhookUrl"), "url")) {
      webhookClient = new WebhookClientBuilder(Objects.requireNonNull(plugin.getConfig().getString("webhookUrl"))).setDaemon(true).build();
    }
    plugin.log("Connected to webhook!");
    if (!plugin.getDataFolder().exists()) {
      //noinspection ResultOfMethodCallIgnored
      plugin.getDataFolder().mkdir();
    }

    Class.forName("org.sqlite.JDBC");
    connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/players.db");
    plugin.log("Connected to database!");
    connection.createStatement().execute("CREATE TABLE IF NOT EXISTS punishments( id integer PRIMARY KEY AUTOINCREMENT, targetUuid text NOT NULL, epoch integer NOT NULL, wait integer NOT NULL, reason text NOT NULL, type text NOT NULL, category text NOT NULL, sev text NOT NULL, staffUuid text NOT NULL, removedUuid text DEFAULT 'null' NOT NULL, removedReason text DEFAULT 'null');");
    connection.createStatement().execute("CREATE TABLE IF NOT EXISTS uuidCache(id integer PRIMARY KEY  AUTOINCREMENT, username text NOT NULL, uuid text NOT NULL);");
    plugin.log("Database tables initialized!");
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    BanToken banToken = getActiveBan(event.getUniqueId());
    if (banToken == null || banToken.getRemovedUuid() != null) {
      return;
    }
    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "" + ChatColor.BOLD + "You have been banned for " + Util.convertString(banToken.getWait() == 0 ? -1 : banToken.getWait() - (System.currentTimeMillis() - banToken.getEpoch())) + " by " + Bukkit.getOfflinePlayer(banToken.getStaffUuid()).getName() + "\n" + ChatColor.WHITE + banToken.getReason() + "\n" + ChatColor.DARK_GREEN + "Appeal by doing in " + ChatColor.GREEN + "!ticket new appeal" + ChatColor.DARK_GREEN + " the " + ChatColor.GREEN + "#mc-support" + ChatColor.DARK_GREEN + " channel in discord");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!isCached(event.getPlayer().getUniqueId())) {
      addCache(event.getPlayer().getUniqueId(), event.getPlayer().getName());
      return;
    }
    updateCache(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    if (warningNotifications.containsKey(event.getPlayer().getUniqueId())) {
      NotificationToken token = warningNotifications.get(event.getPlayer().getUniqueId());
      warningNotifications.remove(event.getPlayer().getUniqueId());
      Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
        event.getPlayer().sendMessage(ChatColor.AQUA + "Punish>> " + ChatColor.GRAY + token.getStaffName() + " issued a friendly warning to you");
        event.getPlayer().sendMessage(ChatColor.AQUA + "Punish>> " + ChatColor.GRAY + ChatColor.BOLD + "Reason: " + ChatColor.RESET + ChatColor.GRAY + token.getReason());
      }, 80);
    }
  }

  private boolean isCached(UUID uuid) {
    try {
      PreparedStatement statement = connection.prepareStatement("SELECT username FROM uuidCache WHERE uuid = ?");
      statement.setString(1, uuid.toString());
      ResultSet set = statement.executeQuery();
      return set.next();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public UUID fetchCache(String username) {
    try {
      PreparedStatement statement = connection.prepareStatement("SELECT uuid FROM uuidCache WHERE username = ? COLLATE NOCASE");
      statement.setString(1, username);
      ResultSet set = statement.executeQuery();
      if (set.next()) {
        return UUID.fromString(set.getString("uuid"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void addCache(UUID uuid, String username) {
    try {
      PreparedStatement statement = connection.prepareStatement("INSERT INTO uuidCache(uuid, username) VALUES (?, ?)");
      statement.setString(1, uuid.toString());
      statement.setString(2, username);
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void updateCache(UUID uuid, String username) {
    try {
      PreparedStatement statement = connection.prepareStatement("UPDATE uuidCache SET username = ? WHERE uuid = ?");
      statement.setString(1, username);
      statement.setString(2, uuid.toString());
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private BanToken getActiveBan(UUID uuid) {
    try {
      PreparedStatement statement = connection.prepareStatement("SELECT * FROM punishments WHERE targetUuid = ? AND type = 'ban' AND (((" + System.currentTimeMillis() + " - epoch) < wait) OR wait = 0) ORDER BY id DESC");
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

  public void addPunishment(PunishUser target, long delay, String reason, String type, String category, String severity, Player staff) {
    try {
      PreparedStatement statement = connection.prepareStatement("INSERT INTO punishments(targetUuid, wait, reason, type, category, sev, staffUuid, epoch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
      statement.setString(1, target.getUuid().toString());
      statement.setLong(2, delay);
      statement.setString(3, reason);
      statement.setString(4, type);
      statement.setString(5, category);
      statement.setString(6, severity);
      statement.setString(7, staff.getUniqueId().toString());
      statement.setString(8, String.valueOf(System.currentTimeMillis()));
      statement.executeUpdate();

      boolean notifiedPlayer = false;
      String line1 = ChatColor.AQUA + "Punish>> " + ChatColor.GRAY + staff.getName() + (type.equals("ban") ? " banned " + target.getName() + " for " + Util.convertString(delay) : " issued a friendly warning to " + target.getName());
      String line2 = ChatColor.AQUA + "Punish>> " + ChatColor.GRAY + ChatColor.BOLD + "Reason: " + ChatColor.RESET + "" + ChatColor.GRAY + reason;
      for (Player player : Bukkit.getOnlinePlayers()) {
        player.sendMessage(line1.replace(player.getName(), "You"));
        player.sendMessage(line2);
        if (player.getName().equals(target.getName())) {
          notifiedPlayer = true;
        }
      }

      if (webhookClient != null) {
        webhookClient.send(ChatColor.stripColor(line1));
        webhookClient.send(ChatColor.stripColor(line2));
      }

      if (type.equals("warning") && !notifiedPlayer) {
        warningNotifications.put(target.getUuid(), new NotificationToken(staff.getName(), reason));
      }

      if (type.equals("ban") && target.getPlayer().isOnline()) {
        Objects.requireNonNull(target.getPlayer().getPlayer()).kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "You have been banned for " + Util.convertString(delay) + " by " + staff.getName() + "\n" + ChatColor.WHITE + reason + "\n" + ChatColor.DARK_GREEN + "Appeal by doing in " + ChatColor.GREEN + "!ticket new appeal" + ChatColor.DARK_GREEN + " the " + ChatColor.GREEN + "#mc-support" + ChatColor.DARK_GREEN + " channel in discord");
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
