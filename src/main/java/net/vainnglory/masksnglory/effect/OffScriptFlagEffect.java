package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class OffScriptFlagEffect extends StatusEffect {
    public OffScriptFlagEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x000000);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
