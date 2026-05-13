package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class WardenEffect extends StatusEffect {
    public WardenEffect() {
        super(StatusEffectCategory.HARMFUL, 0x1A2744);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(ModEffects.PINNING, 400, 3));
        entity.addStatusEffect(new StatusEffectInstance(ModEffects.SEIZED, 400, 0));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 6));
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
    }
}
