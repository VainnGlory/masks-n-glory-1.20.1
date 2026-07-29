package net.vainnglory.masksnglory.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.vainnglory.masksnglory.world.FarlandsHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(NoiseChunkGenerator.class)
public class FarlandsChunkMixin {

    @Shadow @Final protected RegistryEntry<ChunkGeneratorSettings> settings;

    @Inject(method = "populateNoise", at = @At("RETURN"), cancellable = true)
    private void masksnglory$farlandsPopulateNoise(
            Executor executor, Blender blender, NoiseConfig noiseConfig,
            StructureAccessor structureAccessor, Chunk chunk,
            CallbackInfoReturnable<CompletableFuture<Chunk>> cir) {

        boolean isCustomDimension = settings.getKey()
                .map(k -> !"minecraft".equals(k.getValue().getNamespace()))
                .orElse(false);
        if (isCustomDimension) return;

        ChunkPos chunkPos = chunk.getPos();
        if (!FarlandsHelper.touchesFarlands(chunkPos)) return;

        CompletableFuture<Chunk> original = cir.getReturnValue();
        cir.setReturnValue(original.thenApply(c -> {
            int startX = chunkPos.getStartX();
            int startZ = chunkPos.getStartZ();
            int minY = c.getBottomY();
            int maxY = c.getTopY();
            BlockState stone = Blocks.STONE.getDefaultState();
            BlockState air = Blocks.AIR.getDefaultState();
            BlockPos.Mutable mutablePos = new BlockPos.Mutable();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int worldX = startX + x;
                    int worldZ = startZ + z;
                    if (!FarlandsHelper.isInFarlands(worldX, worldZ)) continue;
                    for (int y = minY; y < maxY; y++) {
                        mutablePos.set(worldX, y, worldZ);
                        c.setBlockState(mutablePos, FarlandsHelper.isSolid(worldX, y, worldZ) ? stone : air, false);
                    }
                }
            }

            Heightmap.populateHeightmaps(c, EnumSet.of(
                    Heightmap.Type.OCEAN_FLOOR_WG,
                    Heightmap.Type.WORLD_SURFACE_WG
            ));

            int centerX = startX + 8;
            int centerZ = startZ + 8;

            if (FarlandsHelper.isInFarlands(centerX, centerZ)) {
                long portalSeed = ((long) startX * 341873128712L) ^ ((long) startZ * 132897987541L);
                Random portalRandom = Random.create(portalSeed);
                if (portalRandom.nextInt(300) == 0) {
                    int portalCount = 5 + portalRandom.nextInt(4);
                    for (int i = 0; i < portalCount; i++) {
                        int dx = portalRandom.nextBetween(-12, 12);
                        int dz = portalRandom.nextBetween(-12, 12);
                        boolean floating = portalRandom.nextFloat() < 0.35f;
                        int floatHeight = floating ? (5 + portalRandom.nextInt(14)) : 0;
                        boolean eastWest = (i % 2 == 0);
                        int portalX = centerX + dx;
                        int portalZ = centerZ + dz;
                        int surfaceY = masksnglory$findSurfaceY(portalX, portalZ, minY, maxY);
                        masksnglory$placeFrame(c, mutablePos, new BlockPos(portalX, surfaceY + floatHeight, portalZ), eastWest, startX, startZ, i == 0);
                    }
                }

                long houseSeed = ((long) startX * 123456789L) ^ ((long) startZ * 987654321L);
                Random houseRandom = Random.create(houseSeed);
                if (houseRandom.nextInt(500) == 0) {
                    int houseX = startX + houseRandom.nextInt(10);
                    int houseZ = startZ + houseRandom.nextInt(10);
                    int houseY = masksnglory$findSurfaceY(houseX + 3, houseZ + 2, minY, maxY);
                    if (houseY > minY + 5 && houseY < maxY - 10) {
                        masksnglory$placeHouse(c, mutablePos, houseX, houseY, houseZ, startX, startZ, houseSeed);
                    }
                }
            }

            return c;
        }));
    }

    private int masksnglory$findSurfaceY(int worldX, int worldZ, int minY, int maxY) {
        for (int y = maxY - 1; y >= minY; y--) {
            if (FarlandsHelper.isSolid(worldX, y, worldZ)) return y + 1;
        }
        return minY;
    }

    private void masksnglory$placeHouse(Chunk c, BlockPos.Mutable pos, int baseX, int baseY, int baseZ, int startX, int startZ, long seed) {
        BlockState cobble = Blocks.COBBLESTONE.getDefaultState();
        BlockState planks = Blocks.OAK_PLANKS.getDefaultState();
        BlockState log = Blocks.OAK_LOG.getDefaultState();
        BlockState glass = Blocks.GLASS_PANE.getDefaultState();
        BlockState cobweb = Blocks.COBWEB.getDefaultState();
        BlockState crafting = Blocks.CRAFTING_TABLE.getDefaultState();
        BlockState chest = Blocks.CHEST.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState stairE = Blocks.OAK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST);
        BlockState stairW = Blocks.OAK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST);

        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 5; z++) {
                masksnglory$tryPlace(c, pos, baseX + x, baseY, baseZ + z, cobble, startX, startZ);
            }
        }
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 3; z++) {
                for (int y = 1; y <= 3; y++) {
                    masksnglory$tryPlace(c, pos, baseX + x, baseY + y, baseZ + z, air, startX, startZ);
                }
            }
        }
        for (int y = 1; y <= 3; y++) {
            for (int x = 0; x < 7; x++) {
                masksnglory$tryPlace(c, pos, baseX + x, baseY + y, baseZ, planks, startX, startZ);
                masksnglory$tryPlace(c, pos, baseX + x, baseY + y, baseZ + 4, planks, startX, startZ);
            }
            for (int z = 1; z <= 3; z++) {
                masksnglory$tryPlace(c, pos, baseX, baseY + y, baseZ + z, planks, startX, startZ);
                masksnglory$tryPlace(c, pos, baseX + 6, baseY + y, baseZ + z, planks, startX, startZ);
            }
        }
        for (int y = 1; y <= 3; y++) {
            masksnglory$tryPlace(c, pos, baseX, baseY + y, baseZ, log, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 6, baseY + y, baseZ, log, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX, baseY + y, baseZ + 4, log, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 6, baseY + y, baseZ + 4, log, startX, startZ);
        }
        masksnglory$tryPlace(c, pos, baseX + 2, baseY + 2, baseZ, glass, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 4, baseY + 2, baseZ, glass, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX, baseY + 2, baseZ + 2, glass, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 6, baseY + 2, baseZ + 2, glass, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 2, baseY + 2, baseZ + 4, glass, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 4, baseY + 2, baseZ + 4, glass, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 3, baseY + 1, baseZ + 4, air, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 3, baseY + 2, baseZ + 4, air, startX, startZ);
        for (int z = 0; z < 5; z++) {
            masksnglory$tryPlace(c, pos, baseX, baseY + 4, baseZ + z, stairE, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 1, baseY + 4, baseZ + z, stairE, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 2, baseY + 4, baseZ + z, planks, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 3, baseY + 4, baseZ + z, planks, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 4, baseY + 4, baseZ + z, planks, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 5, baseY + 4, baseZ + z, stairW, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 6, baseY + 4, baseZ + z, stairW, startX, startZ);
        }
        for (int z = 0; z < 5; z++) {
            masksnglory$tryPlace(c, pos, baseX + 2, baseY + 5, baseZ + z, stairE, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 3, baseY + 5, baseZ + z, planks, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 4, baseY + 5, baseZ + z, stairW, startX, startZ);
        }
        for (int gz : new int[]{0, 4}) {
            masksnglory$tryPlace(c, pos, baseX + 2, baseY + 4, baseZ + gz, planks, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 3, baseY + 4, baseZ + gz, planks, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 4, baseY + 4, baseZ + gz, planks, startX, startZ);
            masksnglory$tryPlace(c, pos, baseX + 3, baseY + 5, baseZ + gz, planks, startX, startZ);
        }
        masksnglory$tryPlace(c, pos, baseX + 1, baseY + 3, baseZ + 1, cobweb, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 5, baseY + 3, baseZ + 1, cobweb, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 5, baseY + 3, baseZ + 3, cobweb, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 2, baseY + 1, baseZ, air, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 5, baseY + 3, baseZ + 4, air, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 6, baseY + 2, baseZ + 1, air, startX, startZ);
        masksnglory$tryPlace(c, pos, baseX + 5, baseY + 1, baseZ + 3, crafting, startX, startZ);

        int chestX = baseX + 1;
        int chestY = baseY + 1;
        int chestZ = baseZ + 1;
        if (chestX - startX >= 0 && chestX - startX <= 15 && chestZ - startZ >= 0 && chestZ - startZ <= 15) {
            pos.set(chestX, chestY, chestZ);
            c.setBlockState(pos, chest, false);
            ChestBlockEntity entity = new ChestBlockEntity(new BlockPos(chestX, chestY, chestZ), chest);
            double distFromOrigin = Math.sqrt((double) chestX * chestX + (double) chestZ * chestZ);
            String lootTablePath = distFromOrigin > 70000.0 ? "chests/farlands_house_far" : "chests/farlands_house";
            entity.setLootTable(new Identifier("masks-n-glory", lootTablePath), seed);
            c.setBlockEntity(entity);
        }
    }

    private void masksnglory$placeFrame(Chunk c, BlockPos.Mutable pos, BlockPos base, boolean eastWest, int startX, int startZ, boolean placeMarker) {
        BlockState obsidian = Blocks.OBSIDIAN.getDefaultState();
        if (eastWest) {
            for (int z = 0; z < 4; z++) {
                masksnglory$tryPlace(c, pos, base.getX(), base.getY(), base.getZ() + z, obsidian, startX, startZ);
                masksnglory$tryPlace(c, pos, base.getX(), base.getY() + 4, base.getZ() + z, obsidian, startX, startZ);
            }
            for (int y = 1; y <= 3; y++) {
                masksnglory$tryPlace(c, pos, base.getX(), base.getY() + y, base.getZ(), obsidian, startX, startZ);
                masksnglory$tryPlace(c, pos, base.getX(), base.getY() + y, base.getZ() + 3, obsidian, startX, startZ);
            }
            if (placeMarker && base.getY() - 1 >= c.getBottomY()) {
                masksnglory$tryPlace(c, pos, base.getX(), base.getY() - 1, base.getZ() + 1, Blocks.BARRIER.getDefaultState(), startX, startZ);
            }
        } else {
            for (int x = 0; x < 4; x++) {
                masksnglory$tryPlace(c, pos, base.getX() + x, base.getY(), base.getZ(), obsidian, startX, startZ);
                masksnglory$tryPlace(c, pos, base.getX() + x, base.getY() + 4, base.getZ(), obsidian, startX, startZ);
            }
            for (int y = 1; y <= 3; y++) {
                masksnglory$tryPlace(c, pos, base.getX(), base.getY() + y, base.getZ(), obsidian, startX, startZ);
                masksnglory$tryPlace(c, pos, base.getX() + 3, base.getY() + y, base.getZ(), obsidian, startX, startZ);
            }
            if (placeMarker && base.getY() - 1 >= c.getBottomY()) {
                masksnglory$tryPlace(c, pos, base.getX() + 1, base.getY() - 1, base.getZ(), Blocks.BARRIER.getDefaultState(), startX, startZ);
            }
        }
    }

    private void masksnglory$tryPlace(Chunk c, BlockPos.Mutable pos, int x, int y, int z, BlockState state, int startX, int startZ) {
        int localX = x - startX;
        int localZ = z - startZ;
        if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15) return;
        pos.set(x, y, z);
        c.setBlockState(pos, state, false);
    }
}
