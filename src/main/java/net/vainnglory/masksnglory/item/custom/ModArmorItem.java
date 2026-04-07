package net.vainnglory.masksnglory.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.util.MaskAbilityManager;
import net.vainnglory.masksnglory.util.ModRarities;

public class ModArmorItem extends ArmorItem {
    private final ModRarities rarity;

    public ModArmorItem(ArmorMaterial material, Type type, Settings settings, ModRarities rarity) {
        super(material, type, settings);
        this.rarity = rarity;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text baseName = super.getName(stack);
        return baseName.copy().setStyle(Style.EMPTY.withColor(rarity.color));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient() && entity instanceof PlayerEntity player) {
            ItemStack helmet = player.getInventory().getArmorStack(3);
            if (helmet == stack) {
                MaskAbilityManager.tick(player, this.getMaterial());
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}
