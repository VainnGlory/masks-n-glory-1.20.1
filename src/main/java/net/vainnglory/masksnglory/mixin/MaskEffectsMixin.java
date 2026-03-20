package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.vainnglory.masksnglory.item.ModArmorMaterials;
import net.vainnglory.masksnglory.util.MaskAbilityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MaskEffectsMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void masksnglory$happyFoolsLuck(DamageSource source, float amount,
                                            CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient) return;
        if (!(self instanceof PlayerEntity player)) return;
        if (MaskAbilityManager.getMaskMaterial(player) != ModArmorMaterials.HMASKS) return;

        if (self.getRandom().nextFloat() < 0.2f) {
            self.heal(amount);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void masksnglory$daveSecondWind(DamageSource source, float amount,
                                            CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient) return;
        if (!(self instanceof PlayerEntity player)) return;
        if (MaskAbilityManager.getMaskMaterial(player) != ModArmorMaterials.DVSHARD) return;
        if (self.getHealth() <= 1.0f) return;

        if (amount >= self.getHealth()) {
            ItemStack helmet = player.getInventory().getArmorStack(3);
            NbtCompound nbt = helmet.getOrCreateNbt();
            long lastActivated = nbt.getLong("MNG_SecondWind");
            long currentTime = self.getWorld().getTime();

            if (currentTime - lastActivated > 2400) {
                self.setHealth(1.0f);
                nbt.putLong("MNG_SecondWind", currentTime);
                if (self.getWorld() instanceof ServerWorld world) {
                    world.playSound(null, self.getX(), self.getY(), self.getZ(),
                            SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void masksnglory$ojiIronShell(DamageSource source, float amount,
                                          CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient) return;
        if (!(self instanceof PlayerEntity player)) return;
        if (MaskAbilityManager.getMaskMaterial(player) != ModArmorMaterials.OSHARD) return;
        if (!MaskAbilityManager.ojiEnterGuard(player.getUuid())) return;

        long currentTime = self.getWorld().getTime();
        if (MaskAbilityManager.isOjiFirstHit(player.getUuid(), currentTime)) {
            MaskAbilityManager.ojiRecordHit(player.getUuid(), currentTime);
            boolean result = self.damage(source, amount * 0.2f);
            MaskAbilityManager.ojiExitGuard(player.getUuid());
            cir.setReturnValue(result);
        } else {
            MaskAbilityManager.ojiExitGuard(player.getUuid());
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void masksnglory$corvArrowReduce(DamageSource source, float amount,
                                             CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient) return;
        if (!(self instanceof PlayerEntity player)) return;
        if (MaskAbilityManager.getMaskMaterial(player) != ModArmorMaterials.CSHARD) return;
        if (!(source.getSource() instanceof PersistentProjectileEntity)) return;
        if (!MaskAbilityManager.corvEnterGuard(player.getUuid())) return;

        boolean result = self.damage(source, amount * 0.7f);
        MaskAbilityManager.corvExitGuard(player.getUuid());
        cir.setReturnValue(result);
    }

    @Inject(method = "damage", at = @At("TAIL"))
    private void masksnglory$sadGriefPulse(DamageSource source, float amount,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient) return;
        if (!(self instanceof PlayerEntity player)) return;
        if (MaskAbilityManager.getMaskMaterial(player) != ModArmorMaterials.SMASKS) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;

        double radius = Math.min(8.0, 3.0 + amount * 0.3);
        Box area = new Box(player.getBlockPos()).expand(radius);
        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, area, e -> e != player)) {
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 60, 1, false, true, true));
        }
    }

    @Inject(method = "damage", at = @At("TAIL"))
    private void masksnglory$houndTrackAttacker(DamageSource source, float amount,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient) return;
        if (!(self instanceof PlayerEntity player)) return;
        if (MaskAbilityManager.getMaskMaterial(player) != ModArmorMaterials.HHSHARD) return;

        Entity attacker = source.getAttacker();
        if (attacker != null) {
            MaskAbilityManager.recordHoundAttacker(player.getUuid(), attacker.getUuid());
        }
    }
}


