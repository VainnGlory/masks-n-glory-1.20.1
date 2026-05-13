package net.vainnglory.masksnglory.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.Vanishable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.item.ModToolMaterial;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WardenItem extends SwordItem implements Vanishable {

    public WardenItem(Settings settings) {
        super(ModToolMaterial.RUSTED, -4, -2.8f, settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x659AB5)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient() && attacker instanceof PlayerEntity player) {
            if (!player.getItemCooldownManager().isCoolingDown(this)) {
                target.addStatusEffect(new StatusEffectInstance(ModEffects.WARDEN, 400, 0), attacker);
                player.getItemCooldownManager().set(this, 900);
            }
        }
        stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.masks-n-glory.warden"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}