package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "dropEquipment", at = @At(value = "TAIL"))
    private void dropEshard(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity && getUuidAsString().equals("d1848a30-b4c9-4f64-817d-0d09377b125c")) {
            dropStack(new ItemStack(ModItems.ESHARD));
        }
    }

    @Inject(method = "dropEquipment", at = @At(value = "TAIL"))
    private void dropTshard(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity && getUuidAsString().equals("f8951f83-bfaf-4a75-80bf-000824037387")) {
            dropStack(new ItemStack(ModItems.TSHARD));
        }
    }

    @Inject(method = "dropEquipment", at = @At(value = "TAIL"))
    private void dropPshard(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity && getUuidAsString().equals("4f4c543e-9134-46f2-8e37-f97a7327ee0a")) {
            dropStack(new ItemStack(ModItems.PSHARD));
        }
    }

    @Inject(method = "dropEquipment", at = @At(value = "TAIL"))
    private void dropOshard(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity && getUuidAsString().equals("ee77d450-64e4-40bc-a70d-78bcf91ffebe")) {
            dropStack(new ItemStack(ModItems.OSHARD));
        }
    }

    @Inject(method = "dropEquipment", at = @At(value = "TAIL"))
    private void dropDshard(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity && getUuidAsString().equals("3af214f5-7f9a-464f-9240-dc96148d8bd4")) {
            dropStack(new ItemStack(ModItems.DSHARD));
        }
    }
    @Shadow public abstract Map<StatusEffect, StatusEffectInstance> getActiveStatusEffects();

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);
}
