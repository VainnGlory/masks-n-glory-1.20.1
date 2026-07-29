package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ProjectileEntity.class)
public interface ProjectileOwnerAccessor {
    @Accessor("ownerUuid")
    UUID masksnglory$getOwnerUuid();

    @Accessor("ownerUuid")
    void masksnglory$setOwnerUuid(UUID ownerUuid);
}
