package net.vainnglory.masksnglory.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.io.InputStream;
import java.util.List;

public class MineshaftVaults {
    private static final int SCHEM_SIZE = 48;
    private static final int CENTER_X = 55000;
    private static final int CENTER_Z = 6700;
    private static final int PLACE_Y = 230;
    private static final int NO_CLIP = 1000;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld overworld = server.getWorld(World.OVERWORLD);
            if (overworld == null) return;
            CastlePersistentState state = CastlePersistentState.getOrCreateMineshaftVault(overworld);
            if (state.placed) return;

            int centerChunkX = CENTER_X >> 4;
            int centerChunkZ = CENTER_Z >> 4;
            if (overworld.getChunkManager().getWorldChunk(centerChunkX, centerChunkZ) == null) return;

            try {
                place(overworld, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void place(ServerWorld world, CastlePersistentState state) throws Exception {
        int startX = CENTER_X - SCHEM_SIZE / 2;
        int startZ = CENTER_Z - SCHEM_SIZE / 2;

        InputStream stream = MineshaftVaults.class.getResourceAsStream(
                "/data/masks-n-glory/schematics/farlands_mineshaft.nbt");
        if (stream == null) throw new IllegalStateException("farlands_mineshaft.nbt not found in resources");
        NbtCompound root = NbtIo.readCompressed(stream);
        stream.close();

        int ox = startX;
        int oy = PLACE_Y;
        int oz = startZ;

        List<BlockState> palette = CastleSpawner.readPalette(root);

        System.out.println("[MineshaftVaults] palette size: " + palette.size());
        System.out.println("[MineshaftVaults] blocks count: " +
                (root.contains("blocks") ? root.getList("blocks", NbtElement.COMPOUND_TYPE).size() : 0));
        System.out.println("[MineshaftVaults] entities count: " +
                (root.contains("entities") ? root.getList("entities", NbtElement.COMPOUND_TYPE).size() : 0));

        CastleSpawner.clearFootprint(world, ox, oy, oz, CENTER_X, CENTER_Z, SCHEM_SIZE, SCHEM_SIZE, SCHEM_SIZE, NO_CLIP, NO_CLIP);
        CastleSpawner.placeBlocks(world, root, palette, ox, oy, oz, CENTER_X, CENTER_Z, NO_CLIP, NO_CLIP);
        CastleSpawner.placeEntities(world, root, ox, oy, oz);

        state.placed = true;
        state.markDirty();
    }
}
