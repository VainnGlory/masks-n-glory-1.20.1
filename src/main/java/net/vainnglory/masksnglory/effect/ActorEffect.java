package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ActorEffect extends StatusEffect {
    public ActorEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x6A0DAD);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
