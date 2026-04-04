package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.item.custom.GlaiveItem;

public class RiskEnchantment extends Enchantment {

    public RiskEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override public int getMaxLevel() { return 1; }
    @Override public int getMinPower(int level) { return 30; }
    @Override public int getMaxPower(int level) { return 50; }
    @Override public boolean isTreasure() { return true; }
    @Override public boolean isAvailableForEnchantedBookOffer() { return false; }
    @Override public boolean isAvailableForRandomSelection() { return false; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof GlaiveItem;
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;

            ItemStack weapon = player.getMainHandStack();
            if (!(weapon.getItem() instanceof GlaiveItem)) return ActionResult.PASS;
            if (EnchantmentHelper.getLevel(ModEnchantments.RISK, weapon) <= 0) return ActionResult.PASS;

            target.addStatusEffect(new StatusEffectInstance(ModEffects.ROTTING, 300, 0, false, false, true));

            return ActionResult.PASS;
        });
}
}
