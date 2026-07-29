package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(FishingBobberEntity.class)
public class BobberPersistenceMixin {

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void masksnglory$writeOwner(NbtCompound nbt, CallbackInfo ci) {
        UUID ownerId = ((ProjectileOwnerAccessor)(Object) this).masksnglory$getOwnerUuid();
        if (ownerId != null) {
            nbt.putUuid("MngStasisOwner", ownerId);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void masksnglory$readOwner(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.containsUuid("MngStasisOwner")) {
            ((ProjectileOwnerAccessor)(Object) this).masksnglory$setOwnerUuid(nbt.getUuid("MngStasisOwner"));
        }
    }
}
