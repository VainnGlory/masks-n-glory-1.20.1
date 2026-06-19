package net.vainnglory.masksnglory.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.effect.ModEffects;
import net.vainnglory.masksnglory.item.ModToolMaterial;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public class PanickEdlyCarvedWoodenSword extends SwordItem {

    private static final String INSERTED_KEY = "InsertedItem";
    private static final String COOLDOWN_KEY = "AbilityCooldown";
    private static final String VOID_EXPIRY_KEY = "VoidEnchantExpiry";
    private static final String VOID_ENCHANTS_KEY = "VoidEnchants";

    public static final String ID_HEART_OF_SEA = "minecraft:heart_of_the_sea";
    public static final String ID_ENDER_EYE = "minecraft:ender_eye";
    public static final String ID_GHAST_TEAR = "minecraft:ghast_tear";
    public static final String ID_VOID = "masks-n-glory:void";
    public static final String ID_RUST = "masks-n-glory:rust";

    public PanickEdlyCarvedWoodenSword(Settings settings) {
        super(ModToolMaterial.RUSTED, 1, -2.3f, settings);
    }

    public static boolean hasInserted(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains(INSERTED_KEY);
    }

    public static String getInserted(ItemStack stack) {
        return stack.hasNbt() ? stack.getNbt().getString(INSERTED_KEY) : "";
    }

    private static void setInserted(ItemStack stack, String id) {
        stack.getOrCreateNbt().putString(INSERTED_KEY, id);
    }

    private static boolean isValidInsertId(String id) {
        return id.equals(ID_HEART_OF_SEA) || id.equals(ID_ENDER_EYE) ||
                id.equals(ID_GHAST_TEAR) || id.equals(ID_VOID) || id.equals(ID_RUST);
    }

    private static boolean isOnCooldown(ItemStack stack, World world) {
        if (!stack.hasNbt() || !stack.getNbt().contains(COOLDOWN_KEY)) return false;
        return world.getTime() < stack.getNbt().getLong(COOLDOWN_KEY);
    }

    private static void setCooldown(ItemStack stack, World world, int ticks) {
        stack.getOrCreateNbt().putLong(COOLDOWN_KEY, world.getTime() + ticks);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (hand != Hand.MAIN_HAND) return TypedActionResult.pass(user.getStackInHand(hand));
        ItemStack stack = user.getMainHandStack();

        if (!hasInserted(stack)) {
            ItemStack offhand = user.getOffHandStack();
            String offhandId = Registries.ITEM.getId(offhand.getItem()).toString();
            if (isValidInsertId(offhandId)) {
                if (!world.isClient) {
                    setInserted(stack, offhandId);
                    offhand.decrement(1);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        }

        if (isOnCooldown(stack, world)) return TypedActionResult.fail(stack);
        if (world.isClient) return TypedActionResult.success(stack);

        switch (getInserted(stack)) {
            case ID_HEART_OF_SEA -> { fireGuardianBeam(world, user, stack); setCooldown(stack, world, 60); }
            case ID_ENDER_EYE -> { teleportBehindTarget(world, user, stack); setCooldown(stack, world, 100); }
            case ID_GHAST_TEAR -> { activateGhastScream(world, user, stack); setCooldown(stack, world, 600); }
            case ID_VOID -> { applyVoidEnchants(stack, world, user); setCooldown(stack, world, 600); }
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (hasInserted(stack)) {
            if (getInserted(stack).equals(ID_RUST)) {
                target.clearStatusEffects();
                if (attacker instanceof PlayerEntity player) {
                    player.clearStatusEffects();
                }
            }
            attacker.getWorld().playSound(null,
                    attacker.getX(), attacker.getY(), attacker.getZ(),
                    MasksNGlorySounds.ITEM_PRIDE_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient) {
            int flags = stack.getOrCreateNbt().getInt("HideFlags");
            if ((flags & 2) == 0) {
                stack.getOrCreateNbt().putInt("HideFlags", flags | 2);
            }
            if (stack.getNbt().contains(VOID_EXPIRY_KEY)) {
                if (world.getTime() >= stack.getNbt().getLong(VOID_EXPIRY_KEY)) {
                    clearVoidEnchants(stack);
                }
            }
        }

        if (world.isClient && selected && entity instanceof PlayerEntity player && !hasInserted(stack)) {
            if (world.random.nextInt(400) == 0) {
                world.playSound(player, player.getX(), player.getY(), player.getZ(),
                        MasksNGlorySounds.ITEM_PANICKEDLY_CARVED_SWORD_WHISPER,
                        SoundCategory.PLAYERS, 0.6f, 1.0f);
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("6 Attack Damage").formatted(Formatting.DARK_GREEN));
        tooltip.add(Text.literal("1.7 Attack Speed").formatted(Formatting.DARK_GREEN));
        if (hasInserted(stack)) {
            int color = switch (getInserted(stack)) {
                case ID_HEART_OF_SEA -> 0x27B7F5;
                case ID_ENDER_EYE -> 0x2A7857;
                case ID_RUST -> 0xFCE6A9;
                case ID_VOID -> 0xEAA9FC;
                case ID_GHAST_TEAR -> 0xDBFFFC;
                default -> 0xFFFFFF;
            };
            net.minecraft.item.Item item = Registries.ITEM.get(new Identifier(getInserted(stack)));
            tooltip.add(Text.translatable("item.masks-n-glory.panickedly_carved_wooden_sword.inserted",
                            Text.translatable(item.getTranslationKey()))
                    .styled(style -> style.withColor(TextColor.fromRgb(color))));
        } else {
            tooltip.add(Text.translatable("item.masks-n-glory.panickedly_carved_wooden_sword.empty"));
        }
    }

    private void fireGuardianBeam(World world, PlayerEntity user, ItemStack stack) {
        Vec3d start = user.getEyePos();
        Vec3d look = user.getRotationVec(1.0f);
        Vec3d end = start.add(look.multiply(20));
        Box searchBox = user.getBoundingBox().stretch(look.multiply(20)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityCollision(world, user, start, end, searchBox,
                e -> e instanceof LivingEntity && e != user);
        if (hit == null) return;
        LivingEntity target = (LivingEntity) hit.getEntity();
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 300, 0, false, true));
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.HOSTILE, 1.5f, 1.0f);
        if (world instanceof ServerWorld sw) {
            Vec3d dir = look.normalize();
            double dist = Math.min(start.distanceTo(hit.getPos()), 20);
            DustParticleEffect dust = new DustParticleEffect(new Vector3f(0.0f, 0.85f, 1.0f), 1.0f);
            for (int i = 0; i < (int) (dist * 8); i++) {
                Vec3d p = start.add(dir.multiply(i / 8.0));
                sw.spawnParticles(dust, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);
            }
        }
    }

    private void teleportBehindTarget(World world, PlayerEntity user, ItemStack stack) {
        Vec3d start = user.getEyePos();
        Vec3d look = user.getRotationVec(1.0f);
        Vec3d end = start.add(look.multiply(16));
        Box searchBox = user.getBoundingBox().stretch(look.multiply(16)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityCollision(world, user, start, end, searchBox,
                e -> e instanceof LivingEntity && e != user);
        if (hit == null) return;
        LivingEntity target = (LivingEntity) hit.getEntity();
        Vec3d targetLook = target.getRotationVec(1.0f);
        Vec3d behind = target.getPos().subtract(targetLook.multiply(1.5));
        if (user instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.requestTeleport(behind.x, target.getY(), behind.z);
        }
        world.playSound(null, behind.x, target.getY(), behind.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    private void activateGhastScream(World world, PlayerEntity user, ItemStack stack) {
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_GHAST_HURT, SoundCategory.HOSTILE, 6.0f, 0.8f);
        world.getEntitiesByClass(PlayerEntity.class,
                        user.getBoundingBox().expand(15),
                        p -> p != user && !p.isCreative() && !p.isSpectator())
                .forEach(p -> p.addStatusEffect(
                        new StatusEffectInstance(ModEffects.DEAFENED, 300, 0, false, false)));
    }

    private void applyVoidEnchants(ItemStack stack, World world, PlayerEntity user) {
        clearVoidEnchants(stack);
        List<Enchantment> all = new ArrayList<>();
        Registries.ENCHANTMENT.forEach(e -> { if (e.isAcceptableItem(stack)) all.add(e); });
        Collections.shuffle(all, new Random());
        int count = 2 + world.getRandom().nextInt(2);
        NbtList enchList = stack.getOrCreateNbt().contains("Enchantments", NbtElement.LIST_TYPE)
                ? stack.getNbt().getList("Enchantments", NbtElement.COMPOUND_TYPE)
                : new NbtList();
        NbtList voidList = new NbtList();
        int added = 0;
        for (int i = 0; i < all.size() && added < count; i++) {
            Enchantment ench = all.get(i);
            Identifier id = Registries.ENCHANTMENT.getId(ench);
            if (id == null) continue;
            int level = 1 + world.getRandom().nextInt(Math.max(1, ench.getMaxLevel()));
            NbtCompound enchEntry = new NbtCompound();
            enchEntry.putString("id", id.toString());
            enchEntry.putInt("lvl", level);
            enchList.add(enchEntry);
            NbtCompound voidEntry = new NbtCompound();
            voidEntry.putString("id", id.toString());
            voidEntry.putInt("lvl", level);
            voidList.add(voidEntry);
            added++;
        }
        stack.getOrCreateNbt().put("Enchantments", enchList);
        stack.getOrCreateNbt().put(VOID_ENCHANTS_KEY, voidList);
        stack.getOrCreateNbt().putLong(VOID_EXPIRY_KEY, world.getTime() + 300);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    private void clearVoidEnchants(ItemStack stack) {
        if (!stack.hasNbt() || !stack.getNbt().contains(VOID_ENCHANTS_KEY)) return;
        NbtList voidList = stack.getNbt().getList(VOID_ENCHANTS_KEY, NbtElement.COMPOUND_TYPE);
        NbtList enchList = stack.getNbt().getList("Enchantments", NbtElement.COMPOUND_TYPE);
        for (NbtElement el : voidList) {
            NbtCompound voidEntry = (NbtCompound) el;
            String targetId = voidEntry.getString("id");
            int targetLvl = voidEntry.getInt("lvl");
            for (int i = enchList.size() - 1; i >= 0; i--) {
                NbtCompound e = enchList.getCompound(i);
                if (e.getString("id").equals(targetId) && e.getInt("lvl") == targetLvl) {
                    enchList.remove(i);
                    break;
                }
            }
        }
        stack.getNbt().put("Enchantments", enchList);
        stack.getNbt().remove(VOID_ENCHANTS_KEY);
        stack.getNbt().remove(VOID_EXPIRY_KEY);
    }
}
