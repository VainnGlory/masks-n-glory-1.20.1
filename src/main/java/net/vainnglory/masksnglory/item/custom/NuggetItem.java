package net.vainnglory.masksnglory.item.custom;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.entity.custom.NuggetEntity;

public class NuggetItem extends Item {

    public NuggetItem(FabricItemSettings fabricItemSettings) {
        super(new FabricItemSettings().maxCount(16));
    }

    public static void doItemExplosion(ItemStack stack, World world, Entity entity) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            if (stack.getNbt().getBoolean("activated")) {
                if (!world.isClient) {
                    stack.decrement(1);
                    doExplosion(world, entity);
                }
            }
        }
    }

    public static void doExplosion(World world, Entity entity) {
        if (!world.isClient) {
            world.createExplosion(null, entity.getX(), entity.getY(), entity.getZ(), getExplosionPower(),
                    world.getGameRules().getBoolean(MasksNGlory.DO_PROPERTY_DAMAGE) ? World.ExplosionSourceType.MOB : World.ExplosionSourceType.NONE);
        }
    }

    public static float getExplosionPower() {
        return 0.7f;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        assert stack.getNbt() != null;
        return stack.getNbt().getBoolean("activated");
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!world.isClient) {
            itemStack.getOrCreateNbt().putBoolean("activated", true);
            user.sendMessage(Text.of("The Nugget is Unstable!"), true);
            NuggetEntity nuggetEntity = new NuggetEntity(world, user);
            nuggetEntity.setItem(itemStack);
            nuggetEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
            world.spawnEntity(nuggetEntity);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }
}

    

