package net.vainnglory.masksnglory.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AshChargeItem extends Item {

    private static final Set<UUID> FALL_IMMUNE_PLAYERS = new HashSet<>();
    private static final Set<UUID> AIR_BOOSTED_PLAYERS = new HashSet<>();

    public AshChargeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            boolean onGround = user.isOnGround();

            if (!onGround && AIR_BOOSTED_PLAYERS.contains(user.getUuid())) {
                return TypedActionResult.fail(stack);
            }

            boolean fullPaleArmor = isWearingFullPaleArmor(user);
            double launchStrength = fullPaleArmor ? 1.75 : 1.05;

            user.setVelocity(user.getVelocity().x, launchStrength, user.getVelocity().z);
            user.velocityModified = true;

            setFallImmune(user, true);

            if (!onGround) {
                AIR_BOOSTED_PLAYERS.add(user.getUuid());
            } else {
                AIR_BOOSTED_PLAYERS.remove(user.getUuid());
            }

            ServerWorld serverWorld = (ServerWorld) world;
            double px = user.getX();
            double py = user.getY();
            double pz = user.getZ();

            for (int i = 0; i < 40; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * 1.5;
                double offsetZ = (world.random.nextDouble() - 0.5) * 1.5;
                double speedX = (world.random.nextDouble() - 0.5) * 0.3;
                double speedY = world.random.nextDouble() * 0.4;
                double speedZ = (world.random.nextDouble() - 0.5) * 0.3;
                serverWorld.spawnParticles(
                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        px + offsetX, py, pz + offsetZ,
                        1, speedX, speedY, speedZ, 0.02
                );
            }
            for (int i = 0; i < 25; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * 1.2;
                double offsetZ = (world.random.nextDouble() - 0.5) * 1.2;
                double speedX = (world.random.nextDouble() - 0.5) * 0.5;
                double speedY = world.random.nextDouble() * 0.6;
                double speedZ = (world.random.nextDouble() - 0.5) * 0.5;
                serverWorld.spawnParticles(
                        ParticleTypes.FLAME,
                        px + offsetX, py, pz + offsetZ,
                        1, speedX, speedY, speedZ, 0.01
                );
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0F, 0.8F);

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        return TypedActionResult.success(stack);
    }


    private boolean isWearingFullPaleArmor(PlayerEntity player) {
        ItemStack helmet   = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chest    = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots    = player.getEquippedStack(EquipmentSlot.FEET);

        return helmet.isOf(ModItems.PALE_HELMET)
                && chest.isOf(ModItems.PALE_CHESTPLATE)
                && leggings.isOf(ModItems.PALE_LEGGINGS)
                && boots.isOf(ModItems.PALE_BOOTS);
    }

    public static void setFallImmune(PlayerEntity player, boolean value) {
        if (value) {
            FALL_IMMUNE_PLAYERS.add(player.getUuid());
        } else {
            FALL_IMMUNE_PLAYERS.remove(player.getUuid());
        }
    }

    public static boolean isFallImmune(PlayerEntity player) {
        return FALL_IMMUNE_PLAYERS.contains(player.getUuid());
    }

    public static void onPlayerLanded(PlayerEntity player) {
        FALL_IMMUNE_PLAYERS.remove(player.getUuid());
        AIR_BOOSTED_PLAYERS.remove(player.getUuid());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.masks-n-glory.ash_charge"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}


