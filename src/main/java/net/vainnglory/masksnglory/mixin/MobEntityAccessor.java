package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobEntity.class)
public interface MobEntityAccessor {

    @Accessor("goalSelector")
    GoalSelector getGoalSelector();

    @Invoker("getXpToDrop")
    int invokeGetXpToDrop();
}
