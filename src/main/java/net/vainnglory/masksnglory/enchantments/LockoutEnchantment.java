package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LockoutEnchantment extends Enchantment {

    private static final Map<UUID, Integer> hitCount = new HashMap<>();
    private static final Map<UUID, Long> lastHitTime = new HashMap<>();
    private static final long HIT_TIMEOUT_MS = 2000;
    private static final int HITS_REQUIRED = 6;
    private static final int EFFECT_DURATION = 150;

    public LockoutEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return 10;
    }

    @Override
    public int getMaxPower(int level) {
        return 50;
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

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.RUSTED_SWORD) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != Hand.MAIN_HAND || world.isClient) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;

            ItemStack weapon = player.getMainHandStack();
            if (EnchantmentHelper.getLevel(ModEnchantments.LOCKOUT, weapon) <= 0) return ActionResult.PASS;

            UUID id = player.getUuid();
            long now = System.currentTimeMillis();
            Long lastHit = lastHitTime.get(id);

            if (lastHit != null && (now - lastHit) > HIT_TIMEOUT_MS) {
                hitCount.put(id, 0);
            }

            int count = hitCount.getOrDefault(id, 0) + 1;
            lastHitTime.put(id, now);

            if (count >= HITS_REQUIRED) {
                hitCount.put(id, 0);
                target.addStatusEffect(new StatusEffectInstance(ModEffects.SEIZED, EFFECT_DURATION, 0, false, true, true));
            } else {
                hitCount.put(id, count);
            }

            return ActionResult.PASS;
        });
    }
}
