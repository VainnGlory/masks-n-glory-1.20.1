package net.vainnglory.masksnglory.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.custom.BoneKnifeItem;
import net.vainnglory.masksnglory.util.BoneKnifeParryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(BowItem.class)
public class BowParryMixin {

    @Inject(method = "onStoppedUsing", at = @At("TAIL"))
    private void masksnglory$registerFiredArrow(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (world.isClient) return;
        if (!(user instanceof PlayerEntity player)) return;
        if (!(player.getOffHandStack().getItem() instanceof BoneKnifeItem)) return;

        List<PersistentProjectileEntity> arrows = world.getEntitiesByClass(
                PersistentProjectileEntity.class,
                player.getBoundingBox().expand(5.0),
                e -> e.getOwner() == player
        );

        if (arrows.isEmpty()) return;

        PersistentProjectileEntity closest = arrows.stream()
                .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)))
                .orElse(null);

        if (closest != null) {
            BoneKnifeParryManager.registerArrow(player.getUuid(), closest, world.getTime());
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void masksnglory$checkParry(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient) return;
        if (hand != Hand.MAIN_HAND) return;
        if (!(user.getOffHandStack().getItem() instanceof BoneKnifeItem)) return;

        if (BoneKnifeParryManager.tryParry(user, world)) {
            cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
        }
    }
}

