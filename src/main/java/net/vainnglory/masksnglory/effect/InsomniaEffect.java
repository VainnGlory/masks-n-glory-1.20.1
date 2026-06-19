package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class InsomniaEffect extends StatusEffect {
    public InsomniaEffect() {
        super(StatusEffectCategory.HARMFUL, 0x2D1B69);
    }

    @Override
    public boolean isInstant() {
        return false;
    }
}
