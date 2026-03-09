package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.entity.custom.PaleSteelCoinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(PersistentProjectileEntity.class)
public abstract class CoinRicochetMixin {

    @Shadow protected boolean inGround;

    private static final Map<Integer, Vec3d> PENDING_REDIRECTS = new HashMap<>();
    private static final Map<Integer, LivingEntity> HOMING_TARGETS = new HashMap<>();
    private static final Map<Integer, Integer> HOMING_TICKS = new HashMap<>();
    private static final Map<Integer, Queue<Vec3d>> PENDING_SOUNDS = new HashMap<>();

    private static final double ARROW_SPEED = 3.0;
    private static final int HOMING_DURATION = 20;

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    private void masksnglory$handleCoinBounce(EntityHitResult entityHitResult, CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity)(Object) this;
        if (arrow.getWorld().isClient) return;
        if (!(entityHitResult.getEntity() instanceof PaleSteelCoinEntity startCoin)) return;

        ci.cancel();
        this.inGround = false;

        World world = arrow.getWorld();
        Entity owner = arrow.getOwner();

        Vec3d startCenter = startCoin.getPos().add(0, startCoin.getHeight() * 0.5, 0);
        Box broadSearch = new Box(
                startCenter.x - 64, startCenter.y - 64, startCenter.z - 64,
                startCenter.x + 64, startCenter.y + 64, startCenter.z + 64
        );
        List<PaleSteelCoinEntity> allCoins = new ArrayList<>(
                world.getEntitiesByClass(PaleSteelCoinEntity.class, broadSearch, c -> !c.isRemoved())
        );

        Set<PaleSteelCoinEntity> used      = new HashSet<>();
        PaleSteelCoinEntity      current   = startCoin;
        Vec3d                    lastCenter = startCenter;

        while (current != null) {
            PENDING_SOUNDS.computeIfAbsent(arrow.getId(), k -> new LinkedList<>())
                    .add(new Vec3d(current.getX(), current.getY(), current.getZ()));

            arrow.setDamage(arrow.getDamage() + 1.5);
            arrow.setCritical(true);

            used.add(current);
            lastCenter = current.getPos().add(0, current.getHeight() * 0.5, 0);
            final Vec3d origin = lastCenter;

            current.discard();

            current = allCoins.stream()
                    .filter(c -> !used.contains(c) && !c.isRemoved())
                    .min(Comparator.comparingDouble(c -> c.getPos().squaredDistanceTo(origin)))
                    .orElse(null);
        }

        arrow.setPosition(lastCenter.x, lastCenter.y, lastCenter.z);

        final Vec3d finalOrigin = lastCenter;
        Box finalSearch = new Box(
                finalOrigin.x - 64, finalOrigin.y - 64, finalOrigin.z - 64,
                finalOrigin.x + 64, finalOrigin.y + 64, finalOrigin.z + 64
        );

        LivingEntity finalTarget = findFinalTarget(world, finalSearch, finalOrigin, owner);
        if (finalTarget == null) return;

        Vec3d dir = finalTarget.getEyePos().subtract(finalOrigin).normalize();
        PENDING_REDIRECTS.put(arrow.getId(), dir.multiply(ARROW_SPEED));
        HOMING_TARGETS.put(arrow.getId(), finalTarget);
        HOMING_TICKS.put(arrow.getId(), 0);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void masksnglory$tickRedirect(CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity)(Object) this;
        if (arrow.getWorld().isClient) return;

        Queue<Vec3d> soundQueue = PENDING_SOUNDS.get(arrow.getId());
        if (soundQueue != null && !soundQueue.isEmpty()) {
            Vec3d pos = soundQueue.poll();
            arrow.getWorld().playSound(null, pos.x, pos.y, pos.z,
                    SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.7F, 1.4F);
            if (soundQueue.isEmpty()) PENDING_SOUNDS.remove(arrow.getId());
        }

        Vec3d pending = PENDING_REDIRECTS.remove(arrow.getId());
        if (pending != null) {
            arrow.setVelocity(pending);
            arrow.velocityModified = true;
            this.inGround = false;
            return;
        }

        LivingEntity target = HOMING_TARGETS.get(arrow.getId());
        if (target == null) return;

        int ticks = HOMING_TICKS.getOrDefault(arrow.getId(), 0);
        if (ticks >= HOMING_DURATION || !target.isAlive() || this.inGround || arrow.isRemoved()) {
            HOMING_TARGETS.remove(arrow.getId());
            HOMING_TICKS.remove(arrow.getId());
            PENDING_SOUNDS.remove(arrow.getId());
            return;
        }

        Vec3d dir = target.getEyePos().subtract(arrow.getPos()).normalize();
        arrow.setVelocity(dir.multiply(ARROW_SPEED));
        arrow.velocityModified = true;
        this.inGround = false;
        HOMING_TICKS.put(arrow.getId(), ticks + 1);
    }

    private static LivingEntity findFinalTarget(World world, Box searchArea, Vec3d origin, Entity owner) {
        List<PlayerEntity> players = world.getEntitiesByClass(
                PlayerEntity.class, searchArea,
                p -> p != owner && !p.isSpectator() && p.isAlive()
        );
        if (!players.isEmpty()) {
            return players.stream()
                    .min(Comparator.comparingDouble(p -> p.getPos().squaredDistanceTo(origin)))
                    .orElse(null);
        }
        List<LivingEntity> mobs = world.getEntitiesByClass(
                LivingEntity.class, searchArea,
                m -> !(m instanceof PlayerEntity) && m != owner && m.isAlive()
        );
        return mobs.stream()
                .min(Comparator.comparingDouble(m -> m.getPos().squaredDistanceTo(origin)))
                .orElse(null);
    }
}




