package net.vainnglory.masksnglory.world;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.sound.MasksNGlorySounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarlandsPortalManager {

    private static final Map<ServerWorld, List<CorruptionSequence>> active = new HashMap<>();

    public static void startCorruption(ServerWorld world, Vec3d pos) {
        active.computeIfAbsent(world, k -> new ArrayList<>()).add(new CorruptionSequence(world, pos));
    }

    public static void tick(MinecraftServer server) {
        for (ServerWorld sw : server.getWorlds()) {
            List<CorruptionSequence> list = active.get(sw);
            if (list == null) continue;
            list.removeIf(CorruptionSequence::tick);
        }
    }

    private static class CorruptionSequence {
        private final ServerWorld world;
        private final Vec3d pos;
        private int timer = 100;

        CorruptionSequence(ServerWorld world, Vec3d pos) {
            this.world = world;
            this.pos = pos;
        }

        boolean tick() {
            timer--;

            if (timer % 7 == 0) {
                world.playSound(null, pos.x, pos.y, pos.z,
                        SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS,
                        2.5f, 0.2f + world.random.nextFloat() * 0.4f);
            }
            if (timer % 5 == 0) {
                world.spawnParticles(ParticleTypes.PORTAL,
                        pos.x, pos.y + 1.0, pos.z, 15, 0.3, 0.5, 0.3, 0.5);
            }
            if (timer == 90) {
                world.playSound(null, pos.x, pos.y, pos.z,
                        SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 2.0f, 0.3f);
            }
            if (timer == 75) {
                world.playSound(null, pos.x, pos.y, pos.z,
                        SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.HOSTILE, 2.0f, 0.5f);
            }
            if (timer == 60) {
                world.playSound(null, pos.x, pos.y, pos.z,
                        MasksNGlorySounds.ITEM_PANICKEDLY_CARVED_SWORD_WHISPER, SoundCategory.PLAYERS, 3.0f, 1.0f);
            }
            if (timer == 50) {
                world.playSound(null, pos.x, pos.y, pos.z,
                        SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 2.0f, 0.15f);
                world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.x, pos.y + 1.0, pos.z, 40, 0.5, 0.7, 0.5, 0.3);
            }
            if (timer == 35) {
                world.playSound(null, pos.x, pos.y, pos.z,
                        MasksNGlorySounds.ITEM_PANICKEDLY_CARVED_SWORD_WHISPER, SoundCategory.PLAYERS, 3.0f, 0.8f);
                world.spawnParticles(ParticleTypes.WITCH,
                        pos.x, pos.y + 1.0, pos.z, 20, 0.4, 0.6, 0.4, 0.1);
            }
            if (timer == 15) {
                world.playSound(null, pos.x, pos.y, pos.z,
                        SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 3.0f, 0.1f);
                world.spawnParticles(ParticleTypes.PORTAL,
                        pos.x, pos.y + 1.0, pos.z, 80, 0.8, 1.0, 0.8, 0.8);
                world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.x, pos.y + 1.0, pos.z, 60, 0.8, 1.0, 0.8, 0.8);
                LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                lightning.refreshPositionAfterTeleport(pos.x, pos.y, pos.z);
                lightning.setCosmetic(true);
                world.spawnEntity(lightning);
            }
            if (timer <= 0) {
                world.playSound(null, pos.x, pos.y, pos.z,
                        SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 2.0f, 0.5f);
                world.playSound(null, pos.x, pos.y, pos.z,
                        MasksNGlorySounds.ITEM_PANICKEDLY_CARVED_SWORD_WHISPER, SoundCategory.PLAYERS, 4.0f, 0.6f);
                ItemEntity drop = new ItemEntity(world, pos.x, pos.y + 0.5, pos.z,
                        new ItemStack(ModItems.PANICKEDLY_CARVED_WOODEN_SWORD));
                drop.setVelocity(0.0, 0.3, 0.0);
                world.spawnEntity(drop);
                return true;
            }
            return false;
        }
    }
}
