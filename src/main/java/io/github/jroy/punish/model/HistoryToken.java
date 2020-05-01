package io.github.jroy.punish.model;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class HistoryToken {

  private final ItemStack item;
  private final Integer id;
}
