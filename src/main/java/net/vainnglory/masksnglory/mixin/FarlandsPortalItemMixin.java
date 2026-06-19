package net.vainnglory.masksnglory.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import net.vainnglory.masksnglory.world.FarlandsPortalManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public class FarlandsPortalItemMixin {

    @Inject(method = "onEntityCollision", at = @At("HEAD"))
    private void masksnglory$corruptHurry(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (world.isClient) return;
        if (!(entity instanceof ItemEntity itemEntity)) return;
        if (itemEntity.isRemoved()) return;
        if (itemEntity.getStack().isEmpty()) return;
        if (!itemEntity.getStack().isOf(Items.WOODEN_SWORD)) return;
        if (!itemEntity.getStack().hasCustomName()) return;
        if (!"hurry".equals(itemEntity.getStack().getName().getString())) return;
        if (!FarlandsHelper.isInFarlands(pos.getX(), pos.getZ())) return;

        Vec3d dropPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        itemEntity.discard();
        FarlandsPortalManager.startCorruption((ServerWorld) world, dropPos);
    }
}
