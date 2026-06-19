package net.vainnglory.masksnglory.mixin;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.world.ModDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(NetherPortalBlock.class)
public class VerdantMemoryPortalMixin {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void masksnglory$verdantPortal(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (world.isClient()) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (!world.getRegistryKey().equals(ModDimensions.VERDANT_MEMORY_KEY)) return;
        ci.cancel();
        if (player.hasPortalCooldown()) return;

        UUID id = player.getUuid();
        long currentTick = ((ServerWorld) world).getTime();
        long lastTick = MasksNGlory.verdantPortalLastTick.getOrDefault(id, -2L);

        if (currentTick - lastTick > 1) {
            MasksNGlory.verdantPortalTicks.put(id, 0);
        }
        MasksNGlory.verdantPortalLastTick.put(id, currentTick);

        int ticks = MasksNGlory.verdantPortalTicks.merge(id, 1, Integer::sum);
        if (ticks < 80) return;

        MasksNGlory.verdantPortalTicks.remove(id);
        MasksNGlory.verdantPortalLastTick.remove(id);

        ServerWorld nether = ((ServerWorld) world).getServer().getWorld(World.NETHER);
        if (nether == null) return;

        double netherX = player.getX() / 8.0;
        double netherZ = player.getZ() / 8.0;
        double netherY = nether.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) netherX, (int) netherZ);

        FabricDimensions.teleport(player, nether, new TeleportTarget(
                new Vec3d(netherX, netherY, netherZ),
                Vec3d.ZERO,
                player.getYaw(),
                player.getPitch()
        ));
    }
}
