package net.vainnglory.masksnglory.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.item.custom.GoldenPanItem;
import net.vainnglory.masksnglory.util.CastIronManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class CastIronBlockMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void castIron$absorbHit(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof PlayerEntity player)) return;
        if (player.getWorld().isClient) return;
        if (!(player.getWorld() instanceof ServerWorld sw)) return;

        var weapon = player.getMainHandStack();
        if (!(weapon.getItem() instanceof GoldenPanItem)) return;
        if (EnchantmentHelper.getLevel(ModEnchantments.CAST_IRON, weapon) == 0) return;
        if (!CastIronManager.isBlocking(player)) return;
        if (source.getAttacker() == null) return;

        boolean isProjectile = source.getSource() instanceof net.minecraft.entity.projectile.ProjectileEntity;
        LivingEntity attacker = source.getAttacker() instanceof LivingEntity le ? le : null;

        if (!isProjectile) {
            CastIronManager.absorbHit(player, attacker, sw);
        }

        cir.setReturnValue(false);
    }
}
