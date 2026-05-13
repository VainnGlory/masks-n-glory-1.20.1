package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DamageTracker.class)
public interface DamageTrackerAccessor {
    @Accessor("entity")
    LivingEntity getEntity();
}
