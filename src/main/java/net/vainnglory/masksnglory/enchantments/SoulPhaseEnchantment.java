package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.vainnglory.masksnglory.item.ModItems;

public class SoulPhaseEnchantment extends Enchantment {

    public SoulPhaseEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return 5;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return true;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return true;
    }
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.GLAIVE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == Hand.MAIN_HAND && !world.isClient && entity instanceof LivingEntity target) {
                float cooldown = player.getAttackCooldownProgress(0.0f);
                ItemStack weapon = player.getMainHandStack();
                int level = EnchantmentHelper.getLevel(ModEnchantments.SOUL, weapon);

                if (level > 0 && cooldown >= 0.85f) {
                    Random random = world.getRandom();
                    if (random.nextFloat() < 0.60f) {
                        float amount = 1.5f * level;
                        world.getServer().execute(() -> {
                            target.hurtTime = 0;
                            target.timeUntilRegen = 0;

                            player.heal(amount);

                            DamageSources damageSources = world.getDamageSources();
                            target.damage(damageSources.magic(), amount);
                        });
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}

