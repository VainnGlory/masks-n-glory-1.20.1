package net.vainnglory.masksnglory.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public class SpiteEffect extends StatusEffect {
    public SpiteEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;

        List<MobEntity> nearbyMobs = serverWorld.getEntitiesByClass(
                MobEntity.class,
                entity.getBoundingBox().expand(30),
                e -> e != entity && e.isAlive()
        );

        for (MobEntity mob : nearbyMobs) {
            LivingEntity currentTarget = mob.getTarget();
            boolean alreadyOnSpiteTarget = currentTarget != null
                    && currentTarget.isAlive()
                    && currentTarget.hasStatusEffect(ModEffects.SPITE);

            if (!alreadyOnSpiteTarget) {
                mob.setTarget(entity);
            }
        }
    }
}