package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.vainnglory.masksnglory.item.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemperEnchantment extends Enchantment {

    private static final float MAX_BUILDUP = 6.0f;
    private static final Map<UUID, Long> lastHitTime = new HashMap<>();
    private static final Map<UUID, Boolean> hitThisTick = new HashMap<>();
    private static final Map<UUID, Boolean> swungThisTick = new HashMap<>();

    public TemperEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override public int getMaxLevel() { return 1; }
    @Override public int getMinPower(int level) { return 10; }
    @Override public int getMaxPower(int level) { return 50; }
    @Override public boolean isTreasure() { return false; }
    @Override public boolean isAvailableForEnchantedBookOffer() { return true; }
    @Override public boolean isAvailableForRandomSelection() { return true; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ModItems.PRIDE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    @Override
    public boolean canAccept(Enchantment other) {
        return !(other instanceof IncumbentEnchantment) && !(other instanceof NotorietyEnchantment) && super.canAccept(other);
    }

    public static void onSwing(UUID id) {
        swungThisTick.put(id, true);
    }

    public static void cleanup(UUID id) {
        lastHitTime.remove(id);
        hitThisTick.remove(id);
        swungThisTick.remove(id);
    }

    public static void registerAttackCallback() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;
            ItemStack weapon = player.getMainHandStack();
            if (EnchantmentHelper.getLevel(ModEnchantments.TEMPER, weapon) <= 0) return ActionResult.PASS;

            UUID id = player.getUuid();
            hitThisTick.put(id, true);

            long now = System.currentTimeMillis();
            long last = lastHitTime.getOrDefault(id, 0L);
            float buildup = Math.min(MAX_BUILDUP, (now - last) / 1000.0f);
            lastHitTime.put(id, now);

            if (buildup >= 0.5f) {
                world.getServer().execute(() -> {
                    if (!target.isAlive()) return;
                    target.hurtTime = 0;
                    target.timeUntilRegen = 0;
                    target.damage(target.getDamageSources().playerAttack((PlayerEntity) player), buildup);
                });
            }

            return ActionResult.PASS;
        });
    }

    public static void registerTickCallback() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID id = player.getUuid();
                boolean swung = swungThisTick.getOrDefault(id, false);
                boolean hit = hitThisTick.getOrDefault(id, false);
                if (swung && !hit) {
                    lastHitTime.put(id, System.currentTimeMillis());
                }
                swungThisTick.remove(id);
                hitThisTick.remove(id);
            }
        });
    }
}