package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.vainnglory.masksnglory.block.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class StabShotManager {
    private static final List<PendingStrike> PENDING = new ArrayList<>();
    private static final float EXPLOSION_POWER = 6.0F;
    private static final int STEP_HEIGHT = 2;
    private static final int GUARANTEED_RADIUS = 3;
    private static final int FRINGE_RADIUS = 5;
    private static final double DAMAGE_RADIUS = 6.0;
    private static final float MAX_DAMAGE = 50.0f;
    private static final double KNOCKBACK_STRENGTH = 4.5;
    private static final double KNOCKBACK_UP = 2.8;

    private static final ExplosionBehavior COLUMN_BEHAVIOR = new ExplosionBehavior() {
        @Override
        public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
            if (isIndestructible(state.getBlock())) {
                return false;
            }
            return super.canDestroyBlock(explosion, world, pos, state, power);
        }
    };

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PENDING.removeIf(strike -> {
                strike.ticks--;
                if (strike.ticks > 0) return false;
                detonate(strike.world, strike.x, strike.z);
                return true;
            });
        });
    }

    public static void schedule(ServerWorld world, double x, double z, int delayTicks) {
        PENDING.add(new PendingStrike(world, x, z, delayTicks));
    }

    private static void detonate(ServerWorld world, double x, double z) {
        blastColumn(world, x, z);

        Box searchBox = new Box(
                x - DAMAGE_RADIUS, world.getBottomY(), z - DAMAGE_RADIUS,
                x + DAMAGE_RADIUS, world.getTopY(), z + DAMAGE_RADIUS);
        List<LivingEntity> caught = world.getEntitiesByClass(LivingEntity.class, searchBox, LivingEntity::isAlive);

        for (LivingEntity entity : caught) {
            double dx = entity.getX() - x;
            double dz = entity.getZ() - z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > DAMAGE_RADIUS) continue;

            double falloff = 1.0 - (dist / DAMAGE_RADIUS);
            double normX = dist > 0.001 ? dx / dist : 0.0;
            double normZ = dist > 0.001 ? dz / dist : 0.0;

            entity.damage(world.getDamageSources().explosion(null), (float) (MAX_DAMAGE * falloff));

            Vec3d push = new Vec3d(normX * KNOCKBACK_STRENGTH * falloff, KNOCKBACK_UP * falloff, normZ * KNOCKBACK_STRENGTH * falloff);
            launch(entity, push);
        }
    }

    private static void launch(LivingEntity entity, Vec3d addedVelocity) {
        entity.setVelocity(entity.getVelocity().add(addedVelocity));
        entity.velocityModified = true;
        if (entity instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
        }
    }

    private static void blastColumn(ServerWorld world, double x, double z) {
        int topY = world.getTopY() - 1;
        int bottomY = world.getBottomY();
        int blockX = MathHelper.floor(x);
        int blockZ = MathHelper.floor(z);
        BlockPos.Mutable cursor = new BlockPos.Mutable();

        for (int y = topY; y >= bottomY; y -= STEP_HEIGHT) {
            cursor.set(blockX, y, blockZ);
            if (world.getBlockState(cursor).isAir()) {
                continue;
            }
            world.createExplosion(null, null, COLUMN_BEHAVIOR, x, y, z, EXPLOSION_POWER, false, World.ExplosionSourceType.TNT);
        }

        clearGuaranteedShaft(world, blockX, blockZ, topY, bottomY);
    }

    private static void clearGuaranteedShaft(ServerWorld world, int centerX, int centerZ, int topY, int bottomY) {
        Random random = world.getRandom();
        BlockPos.Mutable cursor = new BlockPos.Mutable();

        for (int y = topY; y >= bottomY; y--) {
            for (int dx = -FRINGE_RADIUS; dx <= FRINGE_RADIUS; dx++) {
                for (int dz = -FRINGE_RADIUS; dz <= FRINGE_RADIUS; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > FRINGE_RADIUS) continue;

                    cursor.set(centerX + dx, y, centerZ + dz);
                    BlockState state = world.getBlockState(cursor);
                    if (state.isAir() || isIndestructible(state.getBlock())) continue;

                    if (dist <= GUARANTEED_RADIUS) {
                        world.removeBlock(cursor, false);
                        continue;
                    }

                    double chance = 1.0 - ((dist - GUARANTEED_RADIUS) / (FRINGE_RADIUS - GUARANTEED_RADIUS));
                    if (random.nextDouble() < chance) {
                        world.removeBlock(cursor, false);
                    }
                }
            }
        }
    }

    private static boolean isIndestructible(Block block) {
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN
                || block == Blocks.RESPAWN_ANCHOR || block == Blocks.END_PORTAL_FRAME
                || block == Blocks.END_PORTAL || block == Blocks.END_GATEWAY
                || block == Blocks.NETHER_PORTAL || block == ModBlocks.GLITCH_BLOCK;
    }

    private static class PendingStrike {
        final ServerWorld world;
        final double x;
        final double z;
        int ticks;

        PendingStrike(ServerWorld world, double x, double z, int ticks) {
            this.world = world;
            this.x = x;
            this.z = z;
            this.ticks = ticks;
        }
    }
}