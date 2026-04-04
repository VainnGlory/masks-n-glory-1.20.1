package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class RottingEffect extends StatusEffect {
    public RottingEffect() {
        super(StatusEffectCategory.HARMFUL, 0x3A2A1A);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
