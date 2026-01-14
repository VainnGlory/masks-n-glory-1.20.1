package net.vainnglory.masksnglory.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public interface ModDeathSource {
    DamageSource getKillSource(LivingEntity target);
}
