package net.vainnglory.masksnglory.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.vainnglory.masksnglory.MasksNGlory;

import java.util.List;
import java.util.Optional;

public class VerdantStoneFeature extends Feature<DefaultFeatureConfig> {
    public static StructureTemplateManager templateManager = null;

    private static final Identifier[] TEMPLATES = {
            new Identifier(MasksNGlory.MOD_ID, "stonesmall"),
            new Identifier(MasksNGlory.MOD_ID, "stonesmall"),
            new Identifier(MasksNGlory.MOD_ID, "stonemedium"),
            new Identifier(MasksNGlory.MOD_ID, "stonemedium"),
            new Identifier(MasksNGlory.MOD_ID, "stonebig")
    };

    public VerdantStoneFeature(Codec<DefaultFeatureConfig> codec) {
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
                )));
        template.get().place(context.getWorld(), origin, origin, placementData, context.getRandom(), 3);
        return true;
    }
}
