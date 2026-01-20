package net.vainnglory.masksnglory.events;


import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class PlayerDeathEffects {

    public static void onPlayerDeath(ServerPlayerEntity player, DamageSource damageSource) {

        if (damageSource.getAttacker() instanceof PlayerEntity) {
            if (player.getUuidAsString().equals("d1848a30-b4c9-4f64-817d-0d09377b125c")) {
                ServerWorld world = player.getServerWorld();
                BlockPos deathPos = player.getBlockPos();


                applyEffectsToNearbyPlayers(world, deathPos, player);

                scheduleDeathEffects(world, deathPos);
            }
        }
    }

    private static void applyEffectsToNearbyPlayers(ServerWorld world, BlockPos deathPos, ServerPlayerEntity dyingPlayer) {

        Box searchBox = new Box(deathPos).expand(30);


        List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(
                ServerPlayerEntity.class,
                searchBox,
                p -> p != dyingPlayer
        );


        for (ServerPlayerEntity nearbyPlayer : nearbyPlayers) {

            nearbyPlayer.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS,
                    2400,
                    1
            ));


            nearbyPlayer.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS,
                    2400,
                    1
            ));


            nearbyPlayer.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WITHER,
                    400,
                    1
            ));
        }
    }

    private static void scheduleDeathEffects(ServerWorld world, BlockPos pos) {

        for (int i = 0; i < 60; i += 10) {
            int delay = i;
            scheduleTask(world, pos, delay, false);
        }


        scheduleTask(world, pos, 60, true);
    }

    private static void scheduleTask(ServerWorld world, BlockPos pos, int delay, boolean isFinal) {
        new Thread(() -> {
            try {
                Thread.sleep(delay * 50);

                world.getServer().execute(() -> {
                    if (!isFinal) {

                        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                        if (lightning != null) {
                            lightning.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                            lightning.setCosmetic(true);
                            world.spawnEntity(lightning);
                        }


                        world.spawnParticles(ParticleTypes.SOUL,
                                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                                50, 2.0, 2.0, 2.0, 0.15);


                        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                30, 1.5, 1.5, 1.5, 0.1);
                    } else {

                        world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                2.0F, World.ExplosionSourceType.NONE);


                        for (ServerPlayerEntity serverPlayer : world.getServer().getPlayerManager().getPlayerList()) {
                            serverPlayer.playSound(SoundEvents.ENTITY_WITHER_SPAWN,
                                    SoundCategory.HOSTILE, 1.0F, 1.0F);
                        }


                        world.spawnParticles(ParticleTypes.SOUL,
                                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                                100, 3.0, 3.0, 3.0, 0.2);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
