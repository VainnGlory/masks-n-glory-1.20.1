package net.vainnglory.masksnglory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.item.custom.PaleWarPickaxeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart head;

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void onSetAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        ItemStack stack = entity.getStackInHand(Hand.MAIN_HAND);
        if (stack.isOf(ModItems.PALE_WAR_PICKAXE) && PaleWarPickaxeItem.isLoaded(stack)) {
            CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, entity.getMainArm() == Arm.RIGHT);
        }
    }
}
