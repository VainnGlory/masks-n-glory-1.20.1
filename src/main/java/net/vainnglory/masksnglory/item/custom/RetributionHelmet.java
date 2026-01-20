package net.vainnglory.masksnglory.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Vanishable;
import net.minecraft.text.Style;
import net.vainnglory.masksnglory.util.FlashEffectPacket;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.util.ModRarities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RetributionHelmet extends ArmorItem {
    private final ModRarities rarity;
    private static final String STORED_DAMAGE_KEY = "StoredDamage";
    private static final float DAMAGE_THRESHOLD = 40.0f;
    private static final int FLASH_RADIUS = 10;
    private static final int BLINDNESS_DURATION = 100;

    public RetributionHelmet(ArmorMaterial material, Settings settings, ModRarities rarity) {
        super(material, Type.HELMET, settings);
        this.rarity = rarity;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text baseName = super.getName(stack);
        return baseName.copy().setStyle(Style.EMPTY.withColor(rarity.color));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        float storedDamage = getStoredDamage(stack);
        tooltip.add(Text.literal("Stored Damage: " + String.format("%.1f", storedDamage) + "/" + DAMAGE_THRESHOLD)
                .formatted(storedDamage >= DAMAGE_THRESHOLD ? Formatting.GOLD : Formatting.GRAY));

        if (storedDamage >= DAMAGE_THRESHOLD) {
            tooltip.add(Text.literal("Ready to unleash! Press V").formatted(Formatting.YELLOW));
        }
    }

    public static void addStoredDamage(ItemStack stack, float damage) {
        if (!(stack.getItem() instanceof RetributionHelmet)) {
            return;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        float current = nbt.getFloat(STORED_DAMAGE_KEY);
        float newValue = Math.min(current + damage, DAMAGE_THRESHOLD * 2);
        nbt.putFloat(STORED_DAMAGE_KEY, newValue);
    }

    public static float getStoredDamage(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof RetributionHelmet)) {
            return 0.0f;
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return 0.0f;
        }

        return nbt.getFloat(STORED_DAMAGE_KEY);
    }

    public static boolean canActivate(ItemStack stack) {
        return getStoredDamage(stack) >= DAMAGE_THRESHOLD;
    }


    public static void playChargeSound(PlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld world) {
            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.BLOCK_BEACON_DEACTIVATE,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.5f
            );

            for (int i = 0; i < 20; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * 0.5;
                double offsetY = world.random.nextDouble() * 0.5;
                double offsetZ = (world.random.nextDouble() - 0.5) * 0.5;

                world.spawnParticles(
                        ParticleTypes.END_ROD,
                        player.getX() + offsetX,
                        player.getY() + 1.5 + offsetY,
                        player.getZ() + offsetZ,
                        1,
                        0, 0.1, 0,
                        0.02
                );
            }

            player.sendMessage(Text.literal("Retribution Helmet fully charged!").formatted(Formatting.GOLD), true);
        }
    }

    public static void activateFlash(ServerPlayerEntity player, ItemStack helmet) {
        if (!canActivate(helmet)) {
            return;
        }

        ServerWorld world = player.getServerWorld();

        NbtCompound nbt = helmet.getOrCreateNbt();
        nbt.putFloat(STORED_DAMAGE_KEY, 0.0f);

        Box area = new Box(
                player.getX() - FLASH_RADIUS,
                player.getY() - FLASH_RADIUS,
                player.getZ() - FLASH_RADIUS,
                player.getX() + FLASH_RADIUS,
                player.getY() + FLASH_RADIUS,
                player.getZ() + FLASH_RADIUS
        );

        List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(
                ServerPlayerEntity.class,
                area,
                p -> p != player && p.squaredDistanceTo(player) <= FLASH_RADIUS * FLASH_RADIUS
        );

        for (ServerPlayerEntity target : nearbyPlayers) {
            target.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS,
                    BLINDNESS_DURATION,
                    0,
                    false,
                    true,
                    true
            ));

            FlashEffectPacket.send(target);
        }

        for (int i = 0; i < 100; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2;
            double offsetY = (world.random.nextDouble() - 0.5) * 2;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2;

            world.spawnParticles(
                    ParticleTypes.FLASH,
                    player.getX() + offsetX,
                    player.getY() + 1 + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, 0, 0, 0
            );

            world.spawnParticles(
                    ParticleTypes.END_ROD,
                    player.getX() + offsetX,
                    player.getY() + 1 + offsetY,
                    player.getZ() + offsetZ,
                    1,
                    offsetX * 0.1, offsetY * 0.1, offsetZ * 0.1,
                    0.1
            );
        }

        world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                SoundCategory.PLAYERS,
                1.0f,
                2.0f
        );
    }
}
