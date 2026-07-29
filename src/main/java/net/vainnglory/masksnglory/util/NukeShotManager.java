package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class NukeShotManager {
    private static final List<PendingStrike> PENDING = new ArrayList<>();
    private static final int RING_COUNT = 6;
    private static final int MIN_RING_TNT = 31;
    private static final int MAX_RING_TNT = 94;
    private static final double MAX_HORIZONTAL_SPEED = 1.0;
    private static final double VERTICAL_LAUNCH_RATIO = 0.198;
    private static final double SPAWN_HEIGHT_OFFSET = 80.0;
    private static final int FUSE_TICKS = 50;
    private static final int FLASH_TNT_COUNT = 6;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PENDING.removeIf(strike -> {
                strike.ticks--;
                if (strike.ticks > 0) return false;
                detonate(strike.world, strike.x, strike.y, strike.z);
                return true;
            });
        });
    }

    public static void schedule(ServerWorld world, double x, double y, double z, int delayTicks) {
        PENDING.add(new PendingStrike(world, x, y, z, delayTicks));
    }

    private static void detonate(ServerWorld world, double x, double y, double z) {
        double spawnY = Math.min(y + SPAWN_HEIGHT_OFFSET, world.getTopY() - 1);

        for (int i = 0; i < FLASH_TNT_COUNT; i++) {
            TntEntity flash = new TntEntity(EntityType.TNT, world);
            flash.setPosition(x, spawnY + 0.5, z);
            flash.setFuse(0);
            flash.setVelocity(0.0, 0.0, 0.0);
            world.spawnEntity(flash);
        }

        for (int ring = 0; ring < RING_COUNT; ring++) {
            double ringFraction = ring / (double) (RING_COUNT - 1);
            double horizontalSpeed = ringFraction * MAX_HORIZONTAL_SPEED;
            double verticalSpeed = horizontalSpeed * VERTICAL_LAUNCH_RATIO;
            int pointsInRing = MIN_RING_TNT + (int) Math.round((MAX_RING_TNT - MIN_RING_TNT) * ringFraction);

            for (int i = 0; i < pointsInRing; i++) {
                double angle = (2.0 * Math.PI * i) / pointsInRing;
                double dirX = MathHelper.cos((float) angle);
                double dirZ = MathHelper.sin((float) angle);

                TntEntity tnt = new TntEntity(EntityType.TNT, world);
                tnt.setPosition(x, spawnY, z);
                tnt.setFuse(FUSE_TICKS);
                tnt.setVelocity(dirX * horizontalSpeed, verticalSpeed, dirZ * horizontalSpeed);
                world.spawnEntity(tnt);
            }
        }
    }

    private static class PendingStrike {
        final ServerWorld world;
        final double x;
        final double y;
        final double z;
        int ticks;

        PendingStrike(ServerWorld world, double x, double y, double z, int ticks) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.ticks = ticks;
        }
    }
}