package net.vainnglory.masksnglory.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.vainnglory.masksnglory.item.ModItems;

public class ArtilleryEnchantment extends Enchantment {

    public ArtilleryEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return 5;
    }

    @Override
    public int getMaxLevel() { return 1; }

    @Override
    public boolean isTreasure() { return false; }

    @Override
    public boolean isAvailableForEnchantedBookOffer() { return true; }

    @Override
    public boolean isAvailableForRandomSelection() { return true; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.PALE_WAR_PICKAXE)
                || stack.isOf(Items.BOOK)
                || stack.isOf(Items.ENCHANTED_BOOK);
    }
}
