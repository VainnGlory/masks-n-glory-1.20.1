package net.vainnglory.masksnglory.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.util.NukeShotManager;

public class NukeShotItem extends Item {
    private static final double RANGE = 128.0;
    private static final int DETONATION_DELAY_TICKS = 40;
    private static final double SELF_TARGET_RADIUS = 1.5;
    private static final double SELF_TARGET_VERTICAL = 2.0;

    public NukeShotItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().formatted(Formatting.ITALIC);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return 0;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xFF0000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            fireNukeShot(serverWorld, user);
            user.sendToolBreakStatus(hand);
            stack.decrement(1);
        }

        user.getItemCooldownManager().set(this, 10);
        return TypedActionResult.success(stack, world.isClient);
    }

    private void fireNukeShot(ServerWorld world, PlayerEntity user) {
        Vec3d eyePos = user.getCameraPosVec(1.0f);
        Vec3d look = user.getRotationVec(1.0f);
        Vec3d endPos = eyePos.add(look.multiply(RANGE));

        BlockHitResult blockHit = world.raycast(new RaycastContext(eyePos, endPos,
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, user));
        Vec3d impactPos = blockHit.getPos();
        double impactDistSq = eyePos.squaredDistanceTo(impactPos);

        Box searchBox = user.getBoundingBox().stretch(look.multiply(RANGE)).expand(1.0);
        EntityHitResult entityHit = ProjectileUtil.raycast(user, eyePos, endPos, searchBox,
                entity -> !entity.isSpectator() && entity.isAlive() && entity != user, RANGE * RANGE);

        if (entityHit != null) {
            double entityDistSq = eyePos.squaredDistanceTo(entityHit.getPos());
            if (entityDistSq < impactDistSq) {
                impactPos = entityHit.getPos();
            }
        }

        if (entityHit == null) {
            double dx = user.getX() - impactPos.x;
            double dz = user.getZ() - impactPos.z;
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            if (horizontalDist <= SELF_TARGET_RADIUS && Math.abs(user.getY() - impactPos.y) <= SELF_TARGET_VERTICAL) {
                impactPos = user.getPos();
            }
        }

        NukeShotManager.schedule(world, impactPos.x, impactPos.y, impactPos.z, DETONATION_DELAY_TICKS);
    }
}
