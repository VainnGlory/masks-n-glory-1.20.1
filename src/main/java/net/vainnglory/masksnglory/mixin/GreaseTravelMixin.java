package net.vainnglory.masksnglory.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.vainnglory.masksnglory.util.GreaseClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public class GreaseTravelMixin {

    @Redirect(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/Block;getSlipperiness()F"))
    private float grease$redirectSlipperiness(Block block) {
        if (MinecraftClient.getInstance().player == (Object)this && GreaseClientState.isGreased()) {
            return 1.1f;
        }
        return block.getSlipperiness();
    }
}
