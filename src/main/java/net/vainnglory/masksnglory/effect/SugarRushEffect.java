package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class SugarRushEffect extends StatusEffect {

    public SugarRushEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xFF6EB4);
        this.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                "a1b2c3d4-e5f6-7890-abcd-ef1234567890", 0.02,
                EntityAttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED,
                "b2c3d4e5-f6a7-8901-bcde-f12345678901", 0.4,
                EntityAttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, amplifier, false, false, false));
    }
}
