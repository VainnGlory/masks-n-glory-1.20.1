package net.vainnglory.masksnglory.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.block.custom.UnlitWallTorchBlock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import net.minecraft.entity.player.PlayerEntity;

public class BlackoutAbilityManager {
    private static final UUID AUTHORIZED_UUID = UUID.fromString("d1848a30-b4c9-4f64-817d-0d09377b125c");
    private static final int RADIUS = 64;
    private static final long AUTO_RESTORE_TICKS = 6000L;
    private static final int POSITIONS_PER_TICK = 50000;

    private static final LinkedHashMap<BlockPos, BlockState> savedBlocks = new LinkedHashMap<>();
    private static ServerWorld savedWorld = null;
    private static MinecraftServer savedServer = null;
    private static boolean active = false;
    private static long activationTick = 0L;

    private static Queue<BlockPos> scanQueue = null;
    private static ServerWorld scanWorld = null;

    public static void toggle(PlayerEntity player) {
        if (!player.getUuid().equals(AUTHORIZED_UUID)) return;
        if (active || scanQueue != null) {
            restore();
        } else {
            activate(player);
        }
    }

    private static void activate(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld world)) return;

        active = true;
        savedWorld = world;
        savedServer = world.getServer();
        activationTick = savedServer.getTicks();

        net.minecraft.sound.SoundEvent ssvSound = Registries.SOUND_EVENT.get(
                new Identifier("ambient.soul_sand_valley.additions"));
        if (ssvSound != null) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ssvSound, SoundCategory.AMBIENT, 3.0f, 0.8f);
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 5.0f, 0.4f);
        BlockPos center = player.getBlockPos();
        BlockPos corner1 = center.add(-RADIUS, -RADIUS, -RADIUS);
        BlockPos corner2 = center.add(RADIUS, RADIUS, RADIUS);

        scanQueue = new ArrayDeque<>();
        for (BlockPos pos : BlockPos.iterate(corner1, corner2)) {
            scanQueue.add(pos.toImmutable());
        }
        scanWorld = world;
    }

    private static void processPosition(BlockPos pos, ServerWorld world) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.TORCH) {
            savedBlocks.put(pos, state);
            world.setBlockState(pos, ModBlocks.UNLIT_TORCH.getDefaultState(), Block.NOTIFY_ALL);
        } else if (block == Blocks.SOUL_TORCH) {
            savedBlocks.put(pos, state);
            world.setBlockState(pos, ModBlocks.UNLIT_SOUL_TORCH.getDefaultState(), Block.NOTIFY_ALL);
        } else if (block == Blocks.WALL_TORCH) {
            savedBlocks.put(pos, state);
            world.setBlockState(pos, ModBlocks.UNLIT_WALL_TORCH.getDefaultState()
                    .with(UnlitWallTorchBlock.FACING, state.get(WallTorchBlock.FACING)), Block.NOTIFY_ALL);
        } else if (block == Blocks.SOUL_WALL_TORCH) {
            savedBlocks.put(pos, state);
            world.setBlockState(pos, ModBlocks.UNLIT_SOUL_WALL_TORCH.getDefaultState()
                    .with(UnlitWallTorchBlock.FACING, state.get(WallTorchBlock.FACING)), Block.NOTIFY_ALL);
        } else if (block == Blocks.LANTERN) {
            savedBlocks.put(pos, state);
            world.setBlockState(pos, ModBlocks.UNLIT_LANTERN.getDefaultState()
                    .with(LanternBlock.HANGING, state.get(LanternBlock.HANGING)), Block.NOTIFY_ALL);
        } else if (block == Blocks.SOUL_LANTERN) {
            savedBlocks.put(pos, state);
            world.setBlockState(pos, ModBlocks.UNLIT_SOUL_LANTERN.getDefaultState()
                    .with(LanternBlock.HANGING, state.get(LanternBlock.HANGING)), Block.NOTIFY_ALL);
        } else if ((block == Blocks.CAMPFIRE || block == Blocks.SOUL_CAMPFIRE)
                && state.get(Properties.LIT)) {
            savedBlocks.put(pos, state);
            world.setBlockState(pos, state.with(Properties.LIT, false), Block.NOTIFY_ALL);
        } else if (state.isIn(BlockTags.CANDLES) && state.contains(Properties.LIT)
                && state.get(Properties.LIT)) {
            savedBlocks.put(pos, state);
            world.setBlockState(pos, state.with(Properties.LIT, false), Block.NOTIFY_ALL);
        }
    }

    public static void restore() {
        if (savedWorld != null) {
            for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
                savedWorld.setBlockState(entry.getKey(), entry.getValue(), Block.NOTIFY_ALL);
            }
        }
        if (savedServer != null) {
            deleteSaveFile(savedServer);
        }
        savedBlocks.clear();
        savedWorld = null;
        savedServer = null;
        active = false;
        activationTick = 0L;
        scanQueue = null;
        scanWorld = null;
    }

    public static void tick(MinecraftServer server) {
        if (scanQueue != null && !scanQueue.isEmpty()) {
            int processed = 0;
            while (!scanQueue.isEmpty() && processed < POSITIONS_PER_TICK) {
                processPosition(scanQueue.poll(), scanWorld);
                processed++;
            }
            if (scanQueue.isEmpty()) {
                scanQueue = null;
                saveState(server);
            }
        }
        if (active && server.getTicks() - activationTick >= AUTO_RESTORE_TICKS) {
            restore();
        }
    }

    private static void saveState(MinecraftServer server) {
        NbtCompound root = new NbtCompound();
        NbtList list = new NbtList();
        for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.put("pos", NbtHelper.fromBlockPos(entry.getKey()));
            entryNbt.put("state", NbtHelper.fromBlockState(entry.getValue()));
            list.add(entryNbt);
        }
        root.put("blocks", list);
        root.putString("world", savedWorld.getRegistryKey().getValue().toString());
        try {
            Path path = new File(server.getRunDirectory(), "blackout_state.nbt").toPath();
            NbtIo.write(root, path.toFile());
        } catch (IOException e) {
            MasksNGlory.LOGGER.error("Failed to save blackout state", e);
        }
    }

    public static void onServerStart(MinecraftServer server) {
        try {
            Path path = new File(server.getRunDirectory(), "blackout_state.nbt").toPath();
            File file = path.toFile();
            if (!file.exists()) return;

            NbtCompound root = NbtIo.read(file);
            if (root == null) return;

            String worldKey = root.getString("world");
            ServerWorld world = null;
            for (ServerWorld w : server.getWorlds()) {
                if (w.getRegistryKey().getValue().toString().equals(worldKey)) {
                    world = w;
                    break;
                }
            }
            if (world == null) return;

            NbtList list = root.getList("blocks", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound entry = list.getCompound(i);
                BlockPos pos = NbtHelper.toBlockPos(entry.getCompound("pos"));
                BlockState state = NbtHelper.toBlockState(
                        Registries.BLOCK.getReadOnlyWrapper(), entry.getCompound("state"));
                world.setBlockState(pos, state, Block.NOTIFY_ALL);
            }

            file.delete();
            MasksNGlory.LOGGER.info("Blackout state restored after server restart.");
        } catch (IOException e) {
            MasksNGlory.LOGGER.error("Failed to restore blackout state on server start", e);
        }
    }

    private static void deleteSaveFile(MinecraftServer server) {
        try {
            new File(server.getRunDirectory(), "blackout_state.nbt").toPath().toFile().delete();
        } catch (Exception e) {
            MasksNGlory.LOGGER.error("Failed to delete blackout save file", e);
        }
    }
}
