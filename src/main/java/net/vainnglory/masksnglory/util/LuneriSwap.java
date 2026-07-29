package net.vainnglory.masksnglory.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.vainnglory.masksnglory.item.ModArmorMaterials;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LuneriSwap {

    private static final double MAX_REACH = 1_000_000.0;
    private static final int COOLDOWN_TICKS = 200;

    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    public static void trigger(ServerPlayerEntity player) {
        if (MaskAbilities.getMaskMaterial(player) != ModArmorMaterials.VSHARD) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;

        UUID id = player.getUuid();
        long now = world.getTime();
        Long lastUse = cooldowns.get(id);
        if (lastUse != null && now - lastUse < COOLDOWN_TICKS) {
            long remaining = (COOLDOWN_TICKS - (now - lastUse)) / 20 + 1;
            player.sendMessage(Text.literal("▸ " + remaining + "s").formatted(Formatting.DARK_GRAY), true);
            return;
        }

        Vec3d eyePos = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d reach = eyePos.add(look.multiply(MAX_REACH));

        LivingEntity target = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : world.iterateEntities()) {
            if (!(entity instanceof LivingEntity candidate) || candidate == player || !candidate.isAlive()) continue;
            Box box = candidate.getBoundingBox().expand(0.3);
            Optional<Vec3d> hit = box.raycast(eyePos, reach);
            if (hit.isPresent()) {
                double distance = eyePos.squaredDistanceTo(hit.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    target = candidate;
                }
            }
        }

        if (target == null) return;

        BlockHitResult blockHit = world.raycast(new RaycastContext(eyePos, target.getEyePos(),
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        if (blockHit.getType() == HitResult.Type.BLOCK
                && eyePos.squaredDistanceTo(blockHit.getPos()) < closestDistance) {
            return;
        }

        double px = player.getX(), py = player.getY(), pz = player.getZ();
        double tx = target.getX(), ty = target.getY(), tz = target.getZ();

        world.playSound(null, px, py, pz, SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        world.playSound(null, tx, ty, tz, SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 1.0f, 1.0f);

        player.requestTeleport(tx, ty, tz);
        if (target instanceof ServerPlayerEntity targetPlayer) {
            targetPlayer.requestTeleport(px, py, pz);
        } else {
            target.refreshPositionAndAngles(px, py, pz, target.getYaw(), target.getPitch());
        }

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0, false, true, true));

        cooldowns.put(id, now);
    }
}
