package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class GreaseManager {

    private static final int GREASE_DURATION_TICKS = 100;
    private static final float MOB_GROUND_BOOST = 1.82f;
    private static final UUID GREASE_SPEED_UUID = UUID.fromString("a2b3c4d5-e6f7-8901-abcd-ef1234567890");

    private static final Map<UUID, Integer> greasedPlayers = new HashMap<>();
    private static final WeakHashMap<LivingEntity, Integer> greasedMobs = new WeakHashMap<>();

    public static void applyGrease(LivingEntity entity) {
        if (entity instanceof ServerPlayerEntity serverPlayer) {
            greasedPlayers.put(serverPlayer.getUuid(), GREASE_DURATION_TICKS);
            GreaseEffectPacket.sendStart(serverPlayer);
        } else if (!(entity instanceof PlayerEntity)) {
            greasedMobs.put(entity, GREASE_DURATION_TICKS);
            var attr = entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (attr != null && attr.getModifier(GREASE_SPEED_UUID) == null) {
                attr.addTemporaryModifier(new EntityAttributeModifier(
                        GREASE_SPEED_UUID, "grease_slide", -0.3, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }

    public static boolean isGreased(PlayerEntity player) {
        return greasedPlayers.containsKey(player.getUuid());
    }

    public static boolean isGreased(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            return greasedPlayers.containsKey(player.getUuid());
        }
        return greasedMobs.containsKey(entity);
    }

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {

            Iterator<Map.Entry<UUID, Integer>> playerIt = greasedPlayers.entrySet().iterator();
            while (playerIt.hasNext()) {
                Map.Entry<UUID, Integer> entry = playerIt.next();
                int ticks = entry.getValue() - 1;

                ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());

                if (ticks <= 0) {
                    playerIt.remove();
                    if (player != null) GreaseEffectPacket.sendStop(player);
                    continue;
                }
                entry.setValue(ticks);

                if (player == null || player.isRemoved()) {
                    playerIt.remove();
                    continue;
                }

                if (ticks % 3 == 0) {
                    ServerWorld sw = player.getServerWorld();
                    sw.spawnParticles(ParticleTypes.DRIPPING_HONEY,
                            player.getX(), player.getY() + player.getHeight() + 0.1, player.getZ(),
                            4, 0.3, 0.1, 0.3, 0.0);
                }
            }

            Iterator<Map.Entry<LivingEntity, Integer>> mobIt = greasedMobs.entrySet().iterator();
            while (mobIt.hasNext()) {
                Map.Entry<LivingEntity, Integer> entry = mobIt.next();
                LivingEntity mob = entry.getKey();
                int ticks = entry.getValue() - 1;

                if (ticks <= 0 || mob.isRemoved()) {
                    mobIt.remove();
                    if (!mob.isRemoved()) {
                        var attr = mob.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                        if (attr != null) attr.removeModifier(GREASE_SPEED_UUID);
                    }
                    continue;
                }
                entry.setValue(ticks);

                Vec3d vel = mob.getVelocity();
                if (mob.isOnGround() && vel.horizontalLengthSquared() > 0.001) {
                    mob.setVelocity(vel.x * MOB_GROUND_BOOST, vel.y, vel.z * MOB_GROUND_BOOST);
                } else if (!mob.isOnGround()) {
                    mob.setVelocity(vel.x * 0.99, vel.y, vel.z * 0.99);
                }
                mob.velocityModified = true;

                if (ticks % 3 == 0 && mob.getWorld() instanceof ServerWorld sw) {
                    sw.spawnParticles(ParticleTypes.DRIPPING_HONEY,
                            mob.getX(), mob.getY() + mob.getHeight() + 0.1, mob.getZ(),
                            4, 0.3, 0.1, 0.3, 0.0);
                }
            }
        });
    }
}
