package net.vainnglory.masksnglory.item.custom;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.ModItems;

public class CookedBonesItem extends Item {

    public CookedBonesItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (hand != Hand.MAIN_HAND) return TypedActionResult.pass(user.getStackInHand(hand));
        if (world.isClient) return TypedActionResult.pass(user.getStackInHand(hand));

        ItemStack offhand = user.getOffHandStack();
        if (!offhand.isOf(ModItems.BONE_KNIFE)) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }

        ItemStack mainHand = user.getStackInHand(hand);
        mainHand.decrement(1);
        offhand.damage(1, user, p -> p.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND));
        user.giveItemStack(new ItemStack(ModItems.MARROWED_BONES, 2));

        return TypedActionResult.success(mainHand);
    }
}
