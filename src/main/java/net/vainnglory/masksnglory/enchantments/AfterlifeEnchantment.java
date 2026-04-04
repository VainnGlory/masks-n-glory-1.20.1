package net.vainnglory.masksnglory.enchantments;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.vainnglory.masksnglory.entity.custom.SoulProjectileEntity;
import net.vainnglory.masksnglory.entity.custom.SoulRavagerEntity;
import net.vainnglory.masksnglory.item.custom.GlaiveItem;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

public class AfterlifeEnchantment extends Enchantment {

    private static final WeakHashMap<PlayerEntity, LivingEntity> lastTargets = new WeakHashMap<>();

    public static final String SOULS_KEY = "AfterlifeSouls";
    public static final String RAVAGER_KEY = "AfterlifeRavagers";

    public AfterlifeEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override public int getMaxLevel() { return 1; }
    @Override public int getMinPower(int level) { return 30; }
    @Override public int getMaxPower(int level) { return 50; }
    @Override public boolean isTreasure() { return true; }
    @Override public boolean isAvailableForEnchantedBookOffer() { return false; }
    @Override public boolean isAvailableForRandomSelection() { return false; }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof GlaiveItem;
    }

    public static LivingEntity getLastTarget(PlayerEntity player) {
        return lastTargets.get(player);
    }

    public static void registerCallbacks() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient && entity instanceof LivingEntity target) {
                ItemStack weapon = player.getMainHandStack();
                if (weapon.getItem() instanceof GlaiveItem
                        && EnchantmentHelper.getLevel(ModEnchantments.AFTERLIFE, weapon) > 0) {
                    lastTargets.put(player, target);
                }
            }
            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient || !(entity instanceof LivingEntity target)) return ActionResult.PASS;
            SoulRavagerEntity ravager = SoulRavagerEntity.getActiveRavager(player.getUuid());
            if (ravager != null) {
                ravager.setTarget(target);
            }
            return ActionResult.PASS;
        });


        ServerLivingEntityEvents.AFTER_DEATH.register((killed, damageSource) -> {
            if (!(damageSource.getAttacker() instanceof PlayerEntity player)) return;
            ItemStack weapon = player.getMainHandStack();
            if (!(weapon.getItem() instanceof GlaiveItem)) return;
            if (EnchantmentHelper.getLevel(ModEnchantments.AFTERLIFE, weapon) <= 0) return;

            NbtCompound nbt = weapon.getOrCreateNbt();

            if (killed instanceof RavagerEntity && !(killed instanceof SoulRavagerEntity)) {
                nbt.putInt(RAVAGER_KEY, nbt.getInt(RAVAGER_KEY) + 1);
            }

            nbt.putInt(SOULS_KEY, nbt.getInt(SOULS_KEY) + 1);

            if (player.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,
                        killed.getX(), killed.getY() + killed.getHeight() / 2.0, killed.getZ(),
                        12, 0.3, 0.3, 0.3, 0.04);
            }
        });

    }

    public static boolean handleUse(PlayerEntity player, ItemStack stack, boolean sneaking) {
        if (player.getWorld().isClient) return false;

        NbtCompound nbt = stack.getOrCreateNbt();

        if (sneaking) {
            int ravagers = nbt.getInt(RAVAGER_KEY);
            if (ravagers <= 0) return false;

            SoulRavagerEntity ravager = new SoulRavagerEntity(player.getWorld(), player);
            ravager.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0f);
            ravager.initialize((ServerWorld) player.getWorld(),
                    player.getWorld().getLocalDifficulty(player.getBlockPos()),
                    net.minecraft.entity.SpawnReason.SPAWN_EGG, null, null);
            player.getWorld().spawnEntity(ravager);
            nbt.putInt(RAVAGER_KEY, ravagers - 1);
            return true;
        } else {
            int souls = nbt.getInt(SOULS_KEY);
            if (souls <= 0) return false;

            LivingEntity target = getLastTarget(player);
            if (target == null || target.isDead() || target.isRemoved()) {
                target = findClosestTarget(player);
            }

            UUID targetUUID = target != null ? target.getUuid() : null;
            SoulProjectileEntity proj = new SoulProjectileEntity(player.getWorld(), player, targetUUID);

            double dx = player.getRotationVec(1.0f).x;
            double dz = player.getRotationVec(1.0f).z;
            proj.setPosition(player.getX() + dx * 0.8, player.getEyeY() - 0.1, player.getZ() + dz * 0.8);
            proj.setVelocity(player, player.getPitch(), player.getYaw(), 0f, 1.5f, 0f);
            player.getWorld().spawnEntity(proj);

            nbt.putInt(SOULS_KEY, souls - 1);
            return true;
        }
    }

    private static LivingEntity findClosestTarget(PlayerEntity player) {
        List<PlayerEntity> players = player.getWorld().getEntitiesByClass(PlayerEntity.class,
                player.getBoundingBox().expand(24), p -> p != player && !p.isDead());
        if (!players.isEmpty()) {
            return players.stream().min(Comparator.comparingDouble(p -> p.squaredDistanceTo(player))).orElse(null);
        }
        List<LivingEntity> mobs = player.getWorld().getEntitiesByClass(LivingEntity.class,
                player.getBoundingBox().expand(24), e -> e != player && !e.isDead() && !(e instanceof PlayerEntity));
        return mobs.stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(player))).orElse(null);
    }
}
