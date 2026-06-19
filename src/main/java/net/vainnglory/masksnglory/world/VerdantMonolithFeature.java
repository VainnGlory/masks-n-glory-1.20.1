package net.vainnglory.masksnglory.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.vainnglory.masksnglory.MasksNGlory;

import java.util.List;
import java.util.Optional;

public class VerdantMonolithFeature extends Feature<DefaultFeatureConfig> {
    public static StructureTemplateManager templateManager = null;

    private static final Identifier TEMPLATE = new Identifier(MasksNGlory.MOD_ID, "monolith");
    private static final Identifier LOOT_TABLE = new Identifier(MasksNGlory.MOD_ID, "chests/monolith");

    public VerdantMonolithFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        if (templateManager == null) return false;
        Optional<StructureTemplate> template = templateManager.getTemplate(TEMPLATE);
        if (template.isEmpty()) return false;
        BlockPos origin = context.getOrigin().down(1);
        BlockRotation rotation = BlockRotation.random(context.getRandom());
        long lootSeed = context.getRandom().nextLong();
        BlockBox bounds = new BlockBox(
                origin.getX() - 16, origin.getY() - 4, origin.getZ() - 16,
                origin.getX() + 32, context.getWorld().getTopY(), origin.getZ() + 32
        );
        StructurePlacementData placementData = new StructurePlacementData()
                .setRotation(rotation)
                .setMirror(BlockMirror.NONE)
                .setIgnoreEntities(true)
                .setBoundingBox(bounds)
                .addProcessor(new BlockIgnoreStructureProcessor(List.of(
                        Blocks.AIR,
                        Blocks.CAVE_AIR,
                        Blocks.VOID_AIR,
                        Blocks.STRUCTURE_BLOCK,
                        Blocks.STRUCTURE_VOID
                )))
                .addProcessor(new StructureProcessor() {
                    @Override
                    public StructureTemplate.StructureBlockInfo process(
                            WorldView world, BlockPos pos, BlockPos pivot,
                            StructureTemplate.StructureBlockInfo originalBlockInfo,
                            StructureTemplate.StructureBlockInfo currentBlockInfo,
                            StructurePlacementData data) {
                        if (!currentBlockInfo.state().isOf(Blocks.CHEST)) return currentBlockInfo;
                        NbtCompound nbt = currentBlockInfo.nbt() != null
                                ? currentBlockInfo.nbt().copy()
                                : new NbtCompound();
                        nbt.putString("LootTable", LOOT_TABLE.toString());
                        nbt.putLong("LootTableSeed", lootSeed);
                        return new StructureTemplate.StructureBlockInfo(
                                currentBlockInfo.pos(), currentBlockInfo.state(), nbt);
                    }

                    @Override
                    protected StructureProcessorType<?> getType() {
                        return StructureProcessorType.BLOCK_IGNORE;
                    }
                });
        template.get().place(context.getWorld(), origin, origin, placementData, context.getRandom(), 3);
        return true;
    }
}
