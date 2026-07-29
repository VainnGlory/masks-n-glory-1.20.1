package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.world.ModDimensions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CastleSpawner {
    private static final int SCHEM_WIDTH = 185;
    private static final int SCHEM_HEIGHT = 148;
    private static final int SCHEM_LENGTH = 221;
    private static final int PLACE_Y = 76;
    private static final int PLATFORM_BASE_Y = 58;
    private static final int ELLIPSE_X = SCHEM_WIDTH / 2 + 20;
    private static final int ELLIPSE_Z = SCHEM_LENGTH / 2 + 20;

    private static final int RUINED_SCHEM_WIDTH = 185;
    private static final int RUINED_SCHEM_HEIGHT = 148;
    private static final int RUINED_SCHEM_LENGTH = 221;
    private static final int RUINED_PLACE_Y = 133;
    private static final int RUINED_PLATFORM_BASE_Y = 94;
    private static final int RUINED_ELLIPSE_X = RUINED_SCHEM_WIDTH / 2 + 30;
    private static final int RUINED_ELLIPSE_Z = RUINED_SCHEM_LENGTH / 2 + 30;
    private static final int RUINED_CENTER_X = 70560;
    private static final int RUINED_CENTER_Z = 7461;

    public static void register() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (!world.getRegistryKey().equals(ModDimensions.VERDANT_MEMORY_KEY)) return;
            CastlePersistentState state = CastlePersistentState.getOrCreate(world);
            if (state.placed) return;
            try {
                placeAll(world, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld overworld = server.getWorld(World.OVERWORLD);
            if (overworld == null) return;
            CastlePersistentState state = CastlePersistentState.getOrCreateRuined(overworld);
            if (state.placed) return;

            int centerChunkX = RUINED_CENTER_X >> 4;
            int centerChunkZ = RUINED_CENTER_Z >> 4;
            if (overworld.getChunkManager().getWorldChunk(centerChunkX, centerChunkZ) == null) return;

            try {
                placeRuinedCastle(overworld, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void placeAll(ServerWorld world, CastlePersistentState state) throws Exception {
        int startX = -SCHEM_WIDTH / 2;
        int startZ = -SCHEM_LENGTH / 2;
        int endX = startX + SCHEM_WIDTH;
        int endZ = startZ + SCHEM_LENGTH;

        for (int cx = (startX - 16) >> 4; cx <= (endX + 16) >> 4; cx++) {
            for (int cz = (startZ - 16) >> 4; cz <= (endZ + 16) >> 4; cz++) {
                world.getChunk(cx, cz);
                world.setChunkForced(cx, cz, true);
            }
        }

        placePlatform(world, 0, 0, PLACE_Y, PLATFORM_BASE_Y, ELLIPSE_X, ELLIPSE_Z);

        InputStream stream = CastleSpawner.class.getResourceAsStream(
                "/data/masks-n-glory/schematics/castle.nbt");
        if (stream == null) throw new IllegalStateException("castle.nbt not found in resources");
        NbtCompound root = NbtIo.readCompressed(stream);
        stream.close();

        int ox = startX;
        int oy = PLACE_Y - 1;
        int oz = startZ;

        List<BlockState> palette = readPalette(root);

        System.out.println("[CastleSpawner] palette size: " + palette.size());
        System.out.println("[CastleSpawner] blocks count: " +
                (root.contains("blocks") ? root.getList("blocks", NbtElement.COMPOUND_TYPE).size() : 0));
        System.out.println("[CastleSpawner] entities count: " +
                (root.contains("entities") ? root.getList("entities", NbtElement.COMPOUND_TYPE).size() : 0));

        clearFootprint(world, ox, oy, oz, 0, 0, SCHEM_WIDTH, SCHEM_HEIGHT, SCHEM_LENGTH, ELLIPSE_X, ELLIPSE_Z);
        placeBlocks(world, root, palette, ox, oy, oz, 0, 0, ELLIPSE_X, ELLIPSE_Z);
        placeEntities(world, root, ox, oy, oz);

        BlockState glitchState = ModBlocks.GLITCH_BLOCK.getDefaultState();
        world.setBlockState(new BlockPos(-11, 78, 0), glitchState, 2);
        world.setBlockState(new BlockPos(-11, 78, -1), glitchState, 2);
        world.setBlockState(new BlockPos(-12, 78, -1), glitchState, 2);
        world.setBlockState(new BlockPos(-12, 78, 0), glitchState, 2);

        state.placed = true;
        state.markDirty();
    }

    private static void placeRuinedCastle(ServerWorld world, CastlePersistentState state) throws Exception {
        int startX = RUINED_CENTER_X - RUINED_SCHEM_WIDTH / 2;
        int startZ = RUINED_CENTER_Z - RUINED_SCHEM_LENGTH / 2;

        placePlatform(world, RUINED_CENTER_X, RUINED_CENTER_Z, RUINED_PLACE_Y, RUINED_PLATFORM_BASE_Y,
                RUINED_ELLIPSE_X, RUINED_ELLIPSE_Z);

        InputStream stream = CastleSpawner.class.getResourceAsStream(
                "/data/masks-n-glory/schematics/ruined_castle.nbt");
        if (stream == null) throw new IllegalStateException("ruined_castle.nbt not found in resources");
        NbtCompound root = NbtIo.readCompressed(stream);
        stream.close();

        int ox = startX;
        int oy = RUINED_PLACE_Y - 1;
        int oz = startZ;

        List<BlockState> palette = readPalette(root);

        System.out.println("[CastleSpawner/Ruined] palette size: " + palette.size());
        System.out.println("[CastleSpawner/Ruined] blocks count: " +
                (root.contains("blocks") ? root.getList("blocks", NbtElement.COMPOUND_TYPE).size() : 0));
        System.out.println("[CastleSpawner/Ruined] entities count: " +
                (root.contains("entities") ? root.getList("entities", NbtElement.COMPOUND_TYPE).size() : 0));

        clearFootprint(world, ox, oy, oz, RUINED_CENTER_X, RUINED_CENTER_Z,
                RUINED_SCHEM_WIDTH, RUINED_SCHEM_HEIGHT, RUINED_SCHEM_LENGTH,
                RUINED_ELLIPSE_X, RUINED_ELLIPSE_Z);
        placeBlocks(world, root, palette, ox, oy, oz, RUINED_CENTER_X, RUINED_CENTER_Z,
                RUINED_ELLIPSE_X, RUINED_ELLIPSE_Z);
        placeEntities(world, root, ox, oy, oz);

        BlockState air = Blocks.AIR.getDefaultState();
        for (int hx = 70567; hx <= 70568; hx++) {
            for (int hz = 7455; hz <= 7456; hz++) {
                for (int hy = world.getBottomY(); hy <= 132; hy++) {
                    world.setBlockState(new BlockPos(hx, hy, hz), air, 2);
                }
            }
        }

        state.placed = true;
        state.markDirty();
    }

    private static void placePlatform(ServerWorld world, int centerX, int centerZ,
                                      int placeY, int platformBaseY, int ellipseX, int ellipseZ) {
        BlockState stone = Blocks.STONE.getDefaultState();
        BlockState grass = Blocks.GRASS_BLOCK.getDefaultState();
        BlockState moss = Blocks.MOSS_BLOCK.getDefaultState();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int lx = -ellipseX; lx <= ellipseX; lx++) {
            for (int lz = -ellipseZ; lz <= ellipseZ; lz++) {
                double ex = (double) lx / ellipseX;
                double ez = (double) lz / ellipseZ;
                if (ex * ex + ez * ez > 1.0) continue;
                int wx = centerX + lx;
                int wz = centerZ + lz;
                for (int y = platformBaseY; y < placeY; y++) {
                    pos.set(wx, y, wz);
                    if (y == placeY - 1) {
                        boolean checker = ((wx + wz) & 3) < 2;
                        world.setBlockState(pos, checker ? grass : moss, 2);
                    } else {
                        world.setBlockState(pos, stone, 2);
                    }
                }
            }
        }
    }

    static void clearFootprint(ServerWorld world, int ox, int oy, int oz,
                               int centerX, int centerZ,
                               int schemWidth, int schemHeight, int schemLength,
                               int ellipseX, int ellipseZ) {
        BlockState air = Blocks.AIR.getDefaultState();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = 0; x < schemWidth; x++) {
            int wx = ox + x;
            double ex = (double)(wx - centerX) / ellipseX;
            for (int z = 0; z < schemLength; z++) {
                int wz = oz + z;
                double ez = (double)(wz - centerZ) / ellipseZ;
                if (ex * ex + ez * ez > 1.0) continue;
                for (int y = 0; y < schemHeight; y++) {
                    pos.set(wx, oy + y, wz);
                    world.setBlockState(pos, air, 2);
                }
            }
        }
    }

    static List<BlockState> readPalette(NbtCompound root) {
        List<BlockState> palette = new ArrayList<>();
        NbtList paletteList = root.getList("palette", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < paletteList.size(); i++) {
            NbtCompound entry = paletteList.getCompound(i);
            String name = entry.getString("Name");
            Block block = Registries.BLOCK.get(new Identifier(name));
            BlockState state = block.getDefaultState();
            if (entry.contains("Properties")) {
                NbtCompound props = entry.getCompound("Properties");
                for (String key : props.getKeys()) {
                    String value = props.getString(key);
                    for (Property<?> prop : state.getProperties()) {
                        if (!prop.getName().equals(key)) continue;
                        state = applyProperty(state, prop, value);
                        break;
                    }
                }
            }
            palette.add(state);
        }
        return palette;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState applyProperty(BlockState state, Property<?> prop, String value) {
        Optional<?> parsed = prop.parse(value);
        if (parsed.isPresent()) {
            return state.with((Property) prop, (Comparable) parsed.get());
        }
        return state;
    }

    static void placeBlocks(ServerWorld world, NbtCompound root, List<BlockState> palette,
                            int ox, int oy, int oz, int centerX, int centerZ,
                            int ellipseX, int ellipseZ) {
        NbtList blocks = root.getList("blocks", NbtElement.COMPOUND_TYPE);
        BlockPos.Mutable pos = new BlockPos.Mutable();
        BlockState grass = Blocks.GRASS_BLOCK.getDefaultState();
        BlockState moss = Blocks.MOSS_BLOCK.getDefaultState();

        for (int i = 0; i < blocks.size(); i++) {
            NbtCompound entry = blocks.getCompound(i);
            NbtList relPos = entry.getList("pos", NbtElement.INT_TYPE);
            int x = relPos.getInt(0);
            int y = relPos.getInt(1);
            int z = relPos.getInt(2);
            int stateIndex = entry.getInt("state");
            if (stateIndex < 0 || stateIndex >= palette.size()) continue;
            BlockState state = palette.get(stateIndex);

            int wx = ox + x;
            int wz = oz + z;
            double ex = (double)(wx - centerX) / ellipseX;
            double ez = (double)(wz - centerZ) / ellipseZ;
            if (ex * ex + ez * ez > 1.0) continue;

            if (y == 0 && state.isOf(Blocks.GRASS_BLOCK)) {
                boolean checker = ((wx + wz) & 3) < 2;
                state = checker ? grass : moss;
            }

            pos.set(wx, oy + y, wz);
            world.setBlockState(pos, state, 2);

            if (entry.contains("nbt")) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be != null) {
                    NbtCompound beNbt = entry.getCompound("nbt").copy();
                    beNbt.putInt("x", wx);
                    beNbt.putInt("y", oy + y);
                    beNbt.putInt("z", wz);
                    be.readNbt(beNbt);
                    be.markDirty();
                }
            }
        }
    }

    static void placeEntities(ServerWorld world, NbtCompound root, int ox, int oy, int oz) {
        if (!root.contains("entities")) return;
        NbtList entities = root.getList("entities", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < entities.size(); i++) {
            NbtCompound entry = entities.getCompound(i);
            if (!entry.contains("nbt")) continue;
            NbtList relPos = entry.getList("pos", NbtElement.DOUBLE_TYPE);
            if (relPos.size() < 3) continue;
            double relX = relPos.getDouble(0);
            double relY = relPos.getDouble(1);
            double relZ = relPos.getDouble(2);

            double wx = ox + relX;
            double wy = oy + relY;
            double wz = oz + relZ;

            NbtCompound entityNbt = entry.getCompound("nbt").copy();
            NbtList newPos = new NbtList();
            newPos.add(NbtDouble.of(wx));
            newPos.add(NbtDouble.of(wy));
            newPos.add(NbtDouble.of(wz));
            entityNbt.put("Pos", newPos);

            if (entry.contains("blockPos")) {
                int[] bp = entry.getIntArray("blockPos");
                if (bp.length == 3) {
                    if (entityNbt.contains("TileX")) entityNbt.putInt("TileX", ox + bp[0]);
                    if (entityNbt.contains("TileY")) entityNbt.putInt("TileY", oy + bp[1]);
                    if (entityNbt.contains("TileZ")) entityNbt.putInt("TileZ", oz + bp[2]);
                }
            }

            Optional<Entity> opt = EntityType.getEntityFromNbt(entityNbt, world);
            opt.ifPresent(world::spawnEntity);
        }
    }
}
