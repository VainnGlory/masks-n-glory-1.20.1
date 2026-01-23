package net.vainnglory.masksnglory.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

public class FollowOwnerAttackGoal extends Goal {
    private final VexEntity vex;
    private final PlayerEntity owner;
    private LivingEntity target;
    private int updateCountdown;

    public FollowOwnerAttackGoal(VexEntity vex, PlayerEntity owner) {
        this.vex = vex;
        this.owner = owner;
        this.setControls(EnumSet.of(Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        LivingEntity ownerTarget = owner.getAttacking();
        if (ownerTarget != null && ownerTarget.isAlive()) {
            this.target = ownerTarget;
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (owner == null || !owner.isAlive()) {
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        this.vex.setTarget(this.target);
        this.updateCountdown = 0;
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        this.updateCountdown = Math.max(this.updateCountdown - 1, 0);

        if (this.updateCountdown <= 0) {
            this.updateCountdown = 10;

            if (owner != null && owner.isAlive()) {
                LivingEntity ownerTarget = owner.getAttacking();
                if (ownerTarget != null && ownerTarget != this.target) {
                    this.target = ownerTarget;
                    this.vex.setTarget(this.target);
                }
            }
        }
    }
}
