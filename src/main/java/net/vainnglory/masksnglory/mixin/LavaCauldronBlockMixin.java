package net.vainnglory.masksnglory.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LavaCauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaCauldronBlock.class)
public class LavaCauldronBlockMixin {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void masksnglory$protectRecipeItems(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof ItemEntity itemEntity)) return;
        ItemStack stack = itemEntity.getStack();
        if (stack.isOf(ModBlocks.PALE_STEEL_BLOCK.asItem())
                || stack.isOf(Items.NETHERITE_SWORD)
                || stack.isOf(Items.CROSSBOW)
                || stack.isOf(ModItems.NUGGET)) {
            ci.cancel();
        }
    }
}
