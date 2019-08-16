package io.github.jroy.punish.gui;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PunishUser {
  private String name;
  private UUID uuid;
  private OfflinePlayer player;
}
