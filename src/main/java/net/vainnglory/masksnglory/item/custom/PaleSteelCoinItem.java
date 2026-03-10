package net.vainnglory.masksnglory.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.entity.custom.PaleSteelCoinEntity;
import net.vainnglory.masksnglory.util.ModRarities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaleSteelCoinItem extends Item {
    private final ModRarities rarity;

    public PaleSteelCoinItem(Settings settings, ModRarities rarity) {
        super(settings);
        this.rarity = rarity;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text baseName = super.getName(stack);
        return baseName.copy().setStyle(Style.EMPTY.withColor(rarity.color));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            PaleSteelCoinEntity coin = new PaleSteelCoinEntity(world, user);
            Vec3d direction = user.getRotationVector();
            coin.setVelocity(direction.x * 0.4, direction.y * 0.4 + 0.2, direction.z * 0.4);
            coin.setPosition(user.getX(), user.getEyeY() - 0.1, user.getZ());
            world.spawnEntity(coin);

            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_CHAIN_HIT, SoundCategory.PLAYERS, 0.6F, 1.2F);

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.masks-n-glory.pale_steel_coin"));
    }
}

