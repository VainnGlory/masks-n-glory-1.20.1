package net.vainnglory.masksnglory.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.vainnglory.masksnglory.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    private static final Map<UUID, Long> marrowCookStart = new HashMap<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private void masksnglory$marrowCooking(CallbackInfo ci) {
        ItemEntity self = (ItemEntity)(Object)this;
        if (self.getWorld().isClient) return;
        if (!self.getStack().isOf(Items.BONE)) return;

        BlockPos pos = self.getBlockPos();
        BlockState state = self.getWorld().getBlockState(pos);
        if (!state.isOf(Blocks.WATER_CAULDRON)) {
            marrowCookStart.remove(self.getUuid());
            return;
        }

        BlockState below = self.getWorld().getBlockState(pos.down());
        boolean hasFire = (below.isOf(Blocks.CAMPFIRE) || below.isOf(Blocks.SOUL_CAMPFIRE))
                && below.get(CampfireBlock.LIT);
        if (!hasFire) {
            marrowCookStart.remove(self.getUuid());
            return;
        }

        long now = self.getWorld().getTime();
        marrowCookStart.computeIfAbsent(self.getUuid(), k -> now);
        if (now - marrowCookStart.get(self.getUuid()) >= 200) {
            marrowCookStart.remove(self.getUuid());
            self.setStack(new ItemStack(ModItems.COOKED_BONES, self.getStack().getCount()));
        }
    }
}
