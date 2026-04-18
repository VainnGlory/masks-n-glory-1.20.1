package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class NullEffect extends StatusEffect {
    public NullEffect() {
        super(StatusEffectCategory.HARMFUL, 0x7B00FF);
    }
}
