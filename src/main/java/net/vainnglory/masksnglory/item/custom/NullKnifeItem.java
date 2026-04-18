package net.vainnglory.masksnglory.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.Vanishable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.enchantments.ExceptionNotCaughtEnchantment;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.util.NullManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NullKnifeItem extends SwordItem implements Vanishable, CustomHitSoundItem {
    private static final Map<UUID, Long> lastUsed = new HashMap<>();
    private static final long COOLDOWN_MS = 30000L;
    private static final int EFFECT_DURATION_TICKS = 200;
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public NullKnifeItem(ToolMaterial toolMaterial, Settings settings) {
        super(toolMaterial, 0, -1.0f, settings);
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 6, EntityAttributeModifier.Operation.ADDITION)
        );
        builder.put(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.2F, EntityAttributeModifier.Operation.ADDITION)
        );
        this.attributeModifiers = builder.build();
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDEB8FF)));
    }

    @Override
    public void playHitSound(PlayerEntity player) {
        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                MasksNGlorySounds.ITEM_BONE_HIT,
                player.getSoundCategory(),
                0.7F,
                (float) (1.0F + player.getRandom().nextGaussian() / 10f)
        );
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (EnchantmentHelper.getLevel(ModEnchantments.EXCEPTION, stack) > 0) {
            tooltip.add(Text.literal("Exception Not Caught"));
        } else {
            tooltip.add(Text.translatable("tooltip.masks-n-glory.null_knife"));
            super.appendTooltip(stack, world, tooltip, context);
        }
    }

    @Override
    public int getEnchantability() {
        return 1;
    }

    private static boolean isOnCooldown(PlayerEntity player) {
        Long last = lastUsed.get(player.getUuid());
        return last != null && System.currentTimeMillis() - last < COOLDOWN_MS;
    }

    private static void setCooldown(PlayerEntity player) {
        lastUsed.put(player.getUuid(), System.currentTimeMillis());
    }

    public static void cleanup(UUID id) {
        lastUsed.remove(id);
    }

    public static void registerCallbacks() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != Hand.MAIN_HAND || world.isClient) return ActionResult.PASS;
            if (!(entity instanceof ServerPlayerEntity target)) return ActionResult.PASS;

            ItemStack weapon = player.getMainHandStack();
            if (!weapon.isOf(ModItems.NULL_KNIFE)) return ActionResult.PASS;

            if (player.getAttackCooldownProgress(0.0f) < 0.9f) return ActionResult.PASS;

            if (EnchantmentHelper.getLevel(ModEnchantments.EXCEPTION, weapon) > 0) {
                ExceptionNotCaughtEnchantment.applyException(target);
            } else {
                if (!isOnCooldown(player)) {
                    NullManager.applyEffect(target, EFFECT_DURATION_TICKS);
                    target.addStatusEffect(new StatusEffectInstance(
                            ModEffects.NULL_EFFECT, EFFECT_DURATION_TICKS, 0, false, true, true));
                    setCooldown(player);
                }
            }

            return ActionResult.PASS;
        });
    }
}
