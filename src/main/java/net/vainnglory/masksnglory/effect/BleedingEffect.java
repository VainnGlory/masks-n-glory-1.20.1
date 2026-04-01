package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.vainnglory.masksnglory.util.ModDamageTypes;

public class BleedingEffect extends StatusEffect {
    public BleedingEffect() {
        super(StatusEffectCategory.HARMFUL, 0xCC0000);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int interval = 40 >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getHealth() > 1.0F) {
            entity.setHealth(entity.getHealth() - 1.0F);
        } else {
            entity.damage(ModDamageTypes.bleeding(entity), 1.0F);
        }
    }
}
