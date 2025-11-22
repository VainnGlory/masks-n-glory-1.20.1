package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.WeakHashMap;

public class SkullBreakerEnchantment extends Enchantment {
    private static final WeakHashMap<LivingEntity, Float> fallStarts = new WeakHashMap<>();

    public SkullBreakerEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }



    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isTreasure() {
        return true;
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
        return stack.isOf(ModItems.GOLDEN_PAN) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == Hand.MAIN_HAND && !world.isClient && entity instanceof LivingEntity) {
                ItemStack weapon = player.getMainHandStack();
                int level = EnchantmentHelper.getLevel(ModEnchantments.SKULL, weapon);

                if (level > 0) {
                    Float startY = fallStarts.get(player);

                    if (startY != null) {
                        float actualFallDistance = Math.max(0, startY - (float)player.getY());
                        fallStarts.remove(player);

                        float bonus = actualFallDistance * 1.40f * level;

                        if (bonus > 0) {
                            float charge = player.getAttackCooldownProgress(0.5f);

                            if (charge > 0.65f) {
                                world.getServer().execute(() -> {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    LivingEntity target = (LivingEntity) entity;
                                    if (target.isAlive()) {
                                        target.hurtTime = 0;
                                        target.timeUntilRegen = 0;
                                        target.damage(world.getDamageSources().playerAttack(player), bonus);
                                    }
                                });
                            }
                        }
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    public static void registerTickCallback() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ItemStack weapon = player.getMainHandStack();
                int level = EnchantmentHelper.getLevel(ModEnchantments.SKULL, weapon);

                if (level > 0) {
                    if (!player.isOnGround() && player.getVelocity().y < -0.08 && !fallStarts.containsKey(player)) {
                        fallStarts.put(player, (float)player.getY());
                    }

                    if (player.isOnGround() && fallStarts.containsKey(player)) {
                        fallStarts.remove(player);
                    }
                } else {
                    fallStarts.remove(player);
                }
            }
        });
    }
}
