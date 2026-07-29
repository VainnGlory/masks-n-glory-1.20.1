package net.vainnglory.masksnglory.block.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.util.ModDamageTypes;
import net.vainnglory.masksnglory.world.ModDimensions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class GlitchBlock extends Block {
    private static final int CHARGE_TICKS = 100;
    private static final int STAB_COOLDOWN_TICKS = 6000;
    private static final int NUKE_COOLDOWN_TICKS = 36000;
    private static final double CANCEL_RADIUS = 5.0;
    private static final double SHAKE_RADIUS = 5.0;
    private static final int SHAKE_INTERVAL = 30;
    private static final double DAMAGE_RADIUS = 4.0;
    private static final int DAMAGE_INTERVAL = 5;
    private static final float DAMAGE_AMOUNT = 8.0f;
    private static final Vec3d RITUAL_CENTER = new Vec3d(-11.5, 78.5, -0.5);
    private static final double[][] BLOCK_POSITIONS = {
            {-11, 78, 0}, {-11, 78, -1}, {-12, 78, -1}, {-12, 78, 0}
    };

    private static final Map<UUID, ChargeSession> charging = new HashMap<>();
    private static long cooldownUntilTick = 0;

    public GlitchBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

        ServerWorld serverWorld = (ServerWorld) world;
        ItemStack held = player.getStackInHand(hand);

        if (held.isOf(Items.ENCHANTED_BOOK) && EnchantmentHelper.get(held).getOrDefault(ModEnchantments.PERIPHERAL, 0) > 0) {
            held.decrement(1);
            ItemStack ascensionBook = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(ascensionBook, new EnchantmentLevelEntry(ModEnchantments.ASCENSION, 1));
            player.giveItemStack(ascensionBook);
            serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, hit.getPos().x, hit.getPos().y + 0.3, hit.getPos().z, 40, 0.3, 0.3, 0.3, 0.15);
            world.playSound(null, pos, SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 1.0f, 1.6f);
            return ActionResult.SUCCESS;
        }

        if (!held.isOf(ModItems.STAB_SHOT) && !held.isOf(ModItems.NUKE_SHOT)) return ActionResult.PASS;

        if (serverWorld.getTime() < cooldownUntilTick) return ActionResult.FAIL;

        UUID id = player.getUuid();
        if (charging.containsKey(id)) return ActionResult.FAIL;

        charging.put(id, new ChargeSession(CHARGE_TICKS, held.getItem()));
        return ActionResult.SUCCESS;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getWorld(ModDimensions.VERDANT_MEMORY_KEY);
            if (world == null) return;

            long currentTick = world.getTime();

            for (ServerPlayerEntity player : world.getPlayers()) {
                double dist = player.getPos().distanceTo(RITUAL_CENTER);

                if (currentTick % SHAKE_INTERVAL == 0 && dist <= SHAKE_RADIUS) {
                    float angle = (float)(Math.random() * 360.0);
                    player.networkHandler.sendPacket(new DamageTiltS2CPacket(player.getId(), angle));
                }

                if (currentTick % DAMAGE_INTERVAL == 0 && dist <= DAMAGE_RADIUS) {
                    applyGlitchDamage(world, player);
                }
            }

            Iterator<Map.Entry<UUID, ChargeSession>> it = charging.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, ChargeSession> entry = it.next();
                UUID id = entry.getKey();
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(id);

                if (player == null || !player.getWorld().getRegistryKey().equals(ModDimensions.VERDANT_MEMORY_KEY)
                        || player.getPos().distanceTo(RITUAL_CENTER) > CANCEL_RADIUS) {
                    it.remove();
                    continue;
                }

                ChargeSession session = entry.getValue();
                session.ticks--;

                spawnGlitchEffects(world, currentTick);

                if (session.ticks <= 0) {
                    boolean consumed = false;
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        ItemStack stack = player.getInventory().getStack(i);
                        if (stack.isOf(session.item)) {
                            stack.decrement(1);
                            consumed = true;
                            break;
                        }
                    }
                    if (consumed) {
                        player.giveItemStack(new ItemStack(session.item, 1));
                        player.giveItemStack(new ItemStack(session.item, 1));
                        int cooldownTicks = session.item == ModItems.NUKE_SHOT ? NUKE_COOLDOWN_TICKS : STAB_COOLDOWN_TICKS;
                        cooldownUntilTick = currentTick + cooldownTicks;
                        world.spawnParticles(ParticleTypes.PORTAL, RITUAL_CENTER.x, RITUAL_CENTER.y + 1, RITUAL_CENTER.z, 120, 0.6, 0.6, 0.6, 0.4);
                        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, RITUAL_CENTER.x, RITUAL_CENTER.y + 1, RITUAL_CENTER.z, 60, 0.4, 0.4, 0.4, 0.2);
                        world.playSound(null, BlockPos.ofFloored(RITUAL_CENTER), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 2.0f, 0.4f);
                    }
                    it.remove();
                }
            }
        });
    }

    private static void applyGlitchDamage(ServerWorld world, ServerPlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) return;
        if (player.getOffHandStack().isOf(ModItems.NULL_KNIFE)) return;

        player.timeUntilRegen = 0;
        player.damage(ModDamageTypes.glitch(player), DAMAGE_AMOUNT);

        world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1.0, player.getZ(),
                12, 0.3, 0.5, 0.3, 0.25);
        world.spawnParticles(ParticleTypes.SQUID_INK,
                player.getX(), player.getY() + 1.0, player.getZ(),
                6, 0.25, 0.4, 0.25, 0.02);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.HOSTILE, 0.6f, 0.4f + (float)(Math.random() * 0.5));
    }

    private static void spawnGlitchEffects(ServerWorld world, long tick) {
        var particle = (tick % 6 < 3) ? ParticleTypes.PORTAL : ParticleTypes.REVERSE_PORTAL;
        for (double[] p : BLOCK_POSITIONS) {
            world.spawnParticles(particle, p[0] + 0.5, p[1] + 0.5, p[2] + 0.5, 4, 0.3, 0.3, 0.3, 0.1);
        }
        if (tick % 12 == 0) {
            world.playSound(null, BlockPos.ofFloored(RITUAL_CENTER), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.4f, 1.4f + (float)(Math.random() * 0.4));
        }
    }

    private static class ChargeSession {
        int ticks;
        final Item item;

        ChargeSession(int ticks, Item item) {
            this.ticks = ticks;
            this.item = item;
        }
    }
}