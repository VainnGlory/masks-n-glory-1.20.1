package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.WeakHashMap;

public class ComboEnchantment extends Enchantment {
    private static final float MAX_ABSORPTION = 14;
    private static final float ABSORPTION_PER_HIT = 1.5f;

    private static final WeakHashMap<PlayerEntity, LivingEntity> currentTargets = new WeakHashMap<>();
    private static final WeakHashMap<PlayerEntity, Integer> hitCounts = new WeakHashMap<>();

    public ComboEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
    @Override
    public int getMinPower(int level) {
        return 25;
    }

    @Override
    public int getMaxPower(int level) {
        return 31;
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
        return stack.isOf(ModItems.BONE_KNIFE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == Hand.MAIN_HAND && !world.isClient && entity instanceof LivingEntity target) {
                float cooldown = player.getAttackCooldownProgress(0.0f);
                ItemStack weapon = player.getMainHandStack();
                int level = EnchantmentHelper.getLevel(ModEnchantments.COMBO, weapon);

                if (level > 0 && cooldown >= 0.2f && player.isOnGround()) {
                    LivingEntity currentTarget = currentTargets.get(player);
                    if (currentTarget != target) {
                        currentTargets.put(player, target);
                        hitCounts.put(player, 1);
                    } else {
                        hitCounts.put(player, hitCounts.getOrDefault(player, 0) + 1);
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    public static void registerKillCallback() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            if (entity instanceof PlayerEntity player && killedEntity instanceof LivingEntity) {
                LivingEntity currentTarget = currentTargets.get(player);
                if (killedEntity == currentTarget) {
                    int hits = hitCounts.getOrDefault(player, 0);
                    if (hits > 0) {
                        float totalAbsorptionToAdd = hits * ABSORPTION_PER_HIT;
                        float currentAbsorption = player.getAbsorptionAmount();
                        float newAbsorption = Math.min(currentAbsorption + totalAbsorptionToAdd, MAX_ABSORPTION);
                        player.setAbsorptionAmount(newAbsorption);

                        hitCounts.remove(player);
                        currentTargets.remove(player);
                    }
                }
            }
        });
    }
}
