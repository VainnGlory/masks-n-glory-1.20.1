package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.List;
import java.util.WeakHashMap;

public class ImpactEnchantment extends Enchantment {
    private static final WeakHashMap<LivingEntity, Float> fallStarts = new WeakHashMap<>();

    public ImpactEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        return stack.isOf(ModItems.GOLDEN_PAN) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == Hand.MAIN_HAND && !world.isClient && entity instanceof LivingEntity target) {
                ItemStack weapon = player.getMainHandStack();
                int shockwaveLevel = EnchantmentHelper.getLevel(ModEnchantments.IMPACT, weapon);
                int axeMaceLevel = EnchantmentHelper.getLevel(ModEnchantments.SKULL, weapon);
                if (shockwaveLevel > 0) {
                    Float startY = fallStarts.get(player);

                    if (startY != null) {
                        float fallDistance = startY - (float) player.getY();
                        fallStarts.remove(player);

                        if (fallDistance >= 7.0f) {
                            float cooldown = player.getAttackCooldownProgress(0.5f);
                            if (cooldown > 0.65f) {
                                float baseDamage = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                                float enchantmentBonus = EnchantmentHelper.getAttackDamage(weapon, target.getGroup());
                                float totalDamage = baseDamage + enchantmentBonus;

                                if (axeMaceLevel > 0) {
                                    float axeMaceBonus = fallDistance * 0.18f * axeMaceLevel;
                                    totalDamage += axeMaceBonus;
                                }

                                DamageSource source = world.getDamageSources().playerAttack(player);
                                boolean hit = target.damage(source, totalDamage);

                                if (hit) {
                                    Vec3d center = target.getPos();
                                    Box area = new Box(center.add(-3, -1, -3), center.add(3, 2, 3));
                                    List<LivingEntity> entities = world.getNonSpectatingEntities(LivingEntity.class, area);

                                    for (LivingEntity nearby : entities) {
                                        if (nearby != target && nearby != player && nearby.isAlive()) {
                                            float nearbyDamage = totalDamage * 0.60f;
                                            if (axeMaceLevel > 0) {
                                                float axeMaceBonus = fallDistance * 0.10f * axeMaceLevel * 0.35f;
                                                nearbyDamage += axeMaceBonus;
                                            }
                                            nearby.damage(source, nearbyDamage);
                                        }
                                    }
                                }
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
                int shockwaveLevel = EnchantmentHelper.getLevel(ModEnchantments.IMPACT, weapon);
                int axeMaceLevel = EnchantmentHelper.getLevel(ModEnchantments.SKULL, weapon);
                if (shockwaveLevel > 0 || axeMaceLevel > 0) {
                    if (!player.isOnGround() && player.getVelocity().y < -0.08 && !fallStarts.containsKey(player)) {
                        fallStarts.put(player, (float) player.getY());
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
