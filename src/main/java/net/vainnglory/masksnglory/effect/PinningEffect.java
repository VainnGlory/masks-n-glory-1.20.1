package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class PinningEffect extends StatusEffect {
    public PinningEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8B4513);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
    }
}

