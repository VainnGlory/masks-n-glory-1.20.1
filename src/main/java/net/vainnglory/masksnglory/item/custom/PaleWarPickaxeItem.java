package net.vainnglory.masksnglory.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.UseAction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.entity.custom.ModEntityTypes;
import net.vainnglory.masksnglory.entity.custom.ShearProjectileEntity;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import net.vainnglory.masksnglory.util.ModRarities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaleWarPickaxeItem extends PickaxeItem implements CustomHitSoundItem {

    public static final int PULL_TIME = 25;
    private static final int MAX_SHOTS = 10;
    private static final int FIRE_INTERVAL = 2;
    private static final int COOLDOWN_TICKS = 100;

    private static final String LOADED_KEY = "MNG_Artillery_Loaded";
    private static final String SHOTS_KEY = "MNG_Artillery_Shots";

    private final ModRarities rarity;

    public PaleWarPickaxeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, ModRarities rarity) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.rarity = rarity;
    }

    public static boolean isLoaded(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.getBoolean(LOADED_KEY);
    }

    private static void setLoaded(ItemStack stack, boolean loaded) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putBoolean(LOADED_KEY, loaded);
        nbt.putBoolean("Charged", loaded);
    }

    private static int getShotsFired(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null ? nbt.getInt(SHOTS_KEY) : 0;
    }

    private static void setShotsFired(ItemStack stack, int shots) {
        stack.getOrCreateNbt().putInt(SHOTS_KEY, shots);
    }

    private static boolean hasShears(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(Items.SHEARS)) return true;
        }
        return false;
    }

    private static boolean consumeShear(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s.isOf(Items.SHEARS)) {
                player.getInventory().removeStack(i, 1);
                return true;
            }
        }
        return false;
    }

    @Override
    public Text getName(ItemStack stack) {
        if (EnchantmentHelper.getLevel(ModEnchantments.ARTILLERY, stack) > 0) {
            return super.getName(stack).copy().styled(style -> style.withColor(0xFF0061));
        }
        return super.getName(stack).copy().setStyle(Style.EMPTY.withColor(rarity.color));
    }

    @Override
    public void playHitSound(PlayerEntity player) {
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                MasksNGlorySounds.ITEM_PALE_HIT, player.getSoundCategory(),
                0.7F, (float) (1.0F + player.getRandom().nextGaussian() / 10f));
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        if (EnchantmentHelper.getLevel(ModEnchantments.ARTILLERY, stack) > 0) {
            if (isLoaded(stack)) return UseAction.NONE;
            return UseAction.CROSSBOW;
        }
        return UseAction.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (EnchantmentHelper.getLevel(ModEnchantments.ARTILLERY, stack) <= 0) {
            return TypedActionResult.pass(stack);
        }
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (!isLoaded(stack) && !hasShears(user)) {
            return TypedActionResult.fail(stack);
        }
        if (!isLoaded(stack) && !world.isClient) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_CROSSBOW_LOADING_START, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void usageTick(World world, net.minecraft.entity.LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient || !(user instanceof PlayerEntity player)) return;
        if (EnchantmentHelper.getLevel(ModEnchantments.ARTILLERY, stack) <= 0) return;

        int usedTicks = getMaxUseTime(stack) - remainingUseTicks;

        if (!isLoaded(stack)) {
            if (usedTicks == PULL_TIME / 2) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            return;
        }

        int shotsFired = getShotsFired(stack);

        if (shotsFired >= MAX_SHOTS) {
            setLoaded(stack, false);
            setShotsFired(stack, 0);
            player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
            player.stopUsingItem();
            return;
        }

        if (usedTicks % FIRE_INTERVAL == 0) {
            fireShear(world, player);
            setShotsFired(stack, shotsFired + 1);
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, net.minecraft.entity.LivingEntity user, int remainingUseTicks) {
        if (world.isClient || !(user instanceof PlayerEntity player)) return;
        if (EnchantmentHelper.getLevel(ModEnchantments.ARTILLERY, stack) <= 0) return;

        if (isLoaded(stack)) {
            return;
        }

        int usedTicks = getMaxUseTime(stack) - remainingUseTicks;
        if (usedTicks >= PULL_TIME) {
            if (!consumeShear(player)) return;
            setLoaded(stack, true);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    private void fireShear(World world, PlayerEntity player) {
        ShearProjectileEntity shear = new ShearProjectileEntity(ModEntityTypes.SHEAR_PROJECTILE_TYPE, world);
        shear.setOwner(player);
        shear.setPosition(player.getX(), player.getEyeY() - 0.1, player.getZ());

        Vec3d look = player.getRotationVec(1.0f).normalize().multiply(3.5);
        double spread = 0.04;
        Vec3d vel = look.add(
                (world.random.nextDouble() - 0.5) * spread,
                (world.random.nextDouble() - 0.5) * spread,
                (world.random.nextDouble() - 0.5) * spread
        );
        shear.setShootingVelocity(vel);
        world.spawnEntity(shear);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS,
                1.0f, 0.85f + world.random.nextFloat() * 0.3f);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (EnchantmentHelper.getLevel(ModEnchantments.ARTILLERY, stack) > 0) {
            if (isLoaded(stack)) {
                tooltip.add(Text.literal("Loaded").setStyle(Style.EMPTY.withColor(0x55FF55)));
            } else {
                tooltip.add(Text.literal("Unloaded").setStyle(Style.EMPTY.withColor(0xFF5555)));
            }
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }
}