package io.github.jroy.punish.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

public class GlowEnchantment extends EnchantmentWrapper {

  public GlowEnchantment() {
    super("glow");
  }

  @Override
  public boolean canEnchantItem(ItemStack item) {
    return true;
  }

  @Override
  public boolean conflictsWith(Enchantment other) {
    return false;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public EnchantmentTarget getItemTarget() {
    return null;
  }

  @Override
  public int getMaxLevel() {
    return 10;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public int getStartLevel() {
    return 1;
  }
}
