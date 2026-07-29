package net.vainnglory.masksnglory.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class AscensionEnchantment extends Enchantment {
    public AscensionEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ARMOR,
                new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override public int getMaxLevel() { return 1; }
    @Override public int getMinPower(int level) { return 1; }
    @Override public boolean isTreasure() { return false; }
    @Override public boolean isAvailableForRandomSelection() { return false; }
    @Override public boolean isAvailableForEnchantedBookOffer() { return false; }
}
