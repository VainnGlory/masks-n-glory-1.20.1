package net.vainnglory.masksnglory.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Inject(method = "tick", at = @At("HEAD"))
    private void masksnglory$warPickaxeCrafting(CallbackInfo ci) {
        ItemEntity self = (ItemEntity)(Object)this;
        if (self.getWorld().isClient) return;
        if (!self.getStack().isOf(ModBlocks.PALE_STEEL_BLOCK.asItem())) return;

        BlockPos pos = self.getBlockPos();
        if (!self.getWorld().getBlockState(pos).isOf(Blocks.LAVA_CAULDRON)) return;

        List<ItemEntity> nearby = self.getWorld().getEntitiesByType(EntityType.ITEM, new Box(pos), e -> true);

        boolean isFirst = nearby.stream()
                .filter(e -> e.getStack().isOf(ModBlocks.PALE_STEEL_BLOCK.asItem()))
                .findFirst()
                .map(e -> e == self)
                .orElse(false);
        if (!isFirst) return;

        ItemEntity swordEntity = null;
        ItemEntity crossbowEntity = null;
        List<ItemEntity> nuggetEntities = new ArrayList<>();
        int nuggetCount = 0;

        for (ItemEntity e : nearby) {
            ItemStack s = e.getStack();
            if (swordEntity == null && s.isOf(Items.NETHERITE_SWORD)) swordEntity = e;
            else if (crossbowEntity == null && s.isOf(Items.CROSSBOW)) crossbowEntity = e;
            else if (s.isOf(ModItems.NUGGET)) {
                nuggetEntities.add(e);
                nuggetCount += s.getCount();
            }
        }

        if (swordEntity == null || crossbowEntity == null || nuggetCount < 16) return;

        if (self.getStack().getCount() > 1) self.getStack().decrement(1);
        else self.discard();

        if (swordEntity.getStack().getCount() > 1) swordEntity.getStack().decrement(1);
        else swordEntity.discard();

        if (crossbowEntity.getStack().getCount() > 1) crossbowEntity.getStack().decrement(1);
        else crossbowEntity.discard();

        int toConsume = 16;
        for (ItemEntity nuggetEntity : nuggetEntities) {
            if (toConsume <= 0) break;
            int count = nuggetEntity.getStack().getCount();
            if (count <= toConsume) {
                toConsume -= count;
                nuggetEntity.discard();
            } else {
                nuggetEntity.getStack().decrement(toConsume);
                toConsume = 0;
            }
        }

        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
        self.getWorld().spawnEntity(new ItemEntity(self.getWorld(), cx, cy, cz, new ItemStack(ModItems.PALE_WAR_PICKAXE)));
        self.getWorld().playSound(null, pos, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 1.5f, 0.5f);
    }
}
