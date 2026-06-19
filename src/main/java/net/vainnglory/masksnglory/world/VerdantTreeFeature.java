package net.vainnglory.masksnglory.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
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

public class VerdantTreeFeature extends Feature<DefaultFeatureConfig> {
    public static StructureTemplateManager templateManager = null;

    private static final Identifier[] TEMPLATES = {
            new Identifier(MasksNGlory.MOD_ID, "treehuge"),
            new Identifier(MasksNGlory.MOD_ID, "treebig"),
            new Identifier(MasksNGlory.MOD_ID, "treemedium"),
            new Identifier(MasksNGlory.MOD_ID, "fern1"),
            new Identifier(MasksNGlory.MOD_ID, "fern2")
    };

    private static final StructureProcessor PERSISTENT_LEAVES = new StructureProcessor() {
        @Override
        public StructureTemplate.StructureBlockInfo process(
                WorldView world, BlockPos pos, BlockPos pivot,
                StructureTemplate.StructureBlockInfo originalBlockInfo,
                StructureTemplate.StructureBlockInfo currentBlockInfo,
                StructurePlacementData data) {
            BlockState state = currentBlockInfo.state();
            if (state.contains(LeavesBlock.PERSISTENT)) {
                return new StructureTemplate.StructureBlockInfo(
                        currentBlockInfo.pos(),
                        state.with(LeavesBlock.PERSISTENT, true),
                        currentBlockInfo.nbt());
            }
            return currentBlockInfo;
        }

        @Override
        protected StructureProcessorType<?> getType() {
            return StructureProcessorType.BLOCK_IGNORE;
        }
    };

    public VerdantTreeFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        if (templateManager == null) return false;
        Identifier id = TEMPLATES[context.getRandom().nextInt(TEMPLATES.length)];
        Optional<StructureTemplate> template = templateManager.getTemplate(id);
        if (template.isEmpty()) return false;
        BlockPos origin = context.getOrigin().down(1);
        BlockRotation rotation = BlockRotation.random(context.getRandom());
        BlockBox bounds = new BlockBox(
                origin.getX() - 16, origin.getY(), origin.getZ() - 16,
                origin.getX() + 31, context.getWorld().getTopY(), origin.getZ() + 31
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
                        Blocks.STRUCTURE_VOID,
                        Blocks.GRASS_BLOCK,
                        Blocks.DIRT,
                        Blocks.COARSE_DIRT,
                        Blocks.ROOTED_DIRT,
                        Blocks.MOSS_BLOCK,
                        Blocks.MOSSY_COBBLESTONE,
                        Blocks.STONE,
                        Blocks.GRAVEL,
                        Blocks.SAND
                )))
                .addProcessor(PERSISTENT_LEAVES);
        template.get().place(context.getWorld(), origin, origin, placementData, context.getRandom(), 3);
        return true;
    }
}