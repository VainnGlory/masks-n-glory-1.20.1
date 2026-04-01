package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class StuntDoubleEffect extends StatusEffect {
    public StuntDoubleEffect() {
        super(StatusEffectCategory.HARMFUL, 0xC0C0C0);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
