package net.vainnglory.masksnglory.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.block.custom.UnlitLanternBlock;
import net.vainnglory.masksnglory.block.custom.UnlitTorchBlock;
import net.vainnglory.masksnglory.block.custom.UnlitWallTorchBlock;

public class ModBlocks {
    public static final Block PALE_STEEL_BLOCK = registerBlock("pale_steel_block",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).sounds(BlockSoundGroup.NETHERITE)));

    public static final Block PALE_STEEL_TILES = registerBlock("pale_steel_tiles",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).sounds(BlockSoundGroup.NETHERITE)));

    public static final Block CRIMPED_CHISELED_PALE_STEEL_BLOCK = registerBlock("crimped_chiseled_pale_steel_block",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).sounds(BlockSoundGroup.NETHERITE)));

    public static final Block CHISELED_PALE_STEEL_BLOCK = registerBlock("chiseled_pale_steel_block",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).sounds(BlockSoundGroup.NETHERITE)));


    public static final Block UNLIT_TORCH = registerBlockNoItem("unlit_torch",
            new UnlitTorchBlock(FabricBlockSettings.copyOf(Blocks.TORCH).luminance(state -> 0).dropsNothing()));

    public static final Block UNLIT_SOUL_TORCH = registerBlockNoItem("unlit_soul_torch",
            new UnlitTorchBlock(FabricBlockSettings.copyOf(Blocks.SOUL_TORCH).luminance(state -> 0).dropsNothing()));

    public static final Block UNLIT_WALL_TORCH = registerBlockNoItem("unlit_wall_torch",
            new UnlitWallTorchBlock(FabricBlockSettings.copyOf(Blocks.WALL_TORCH).luminance(state -> 0).dropsNothing()));

    public static final Block UNLIT_SOUL_WALL_TORCH = registerBlockNoItem("unlit_soul_wall_torch",
            new UnlitWallTorchBlock(FabricBlockSettings.copyOf(Blocks.SOUL_WALL_TORCH).luminance(state -> 0).dropsNothing()));

    public static final Block UNLIT_LANTERN = registerBlockNoItem("unlit_lantern",
            new UnlitLanternBlock(FabricBlockSettings.copyOf(Blocks.LANTERN).luminance(state -> 0).dropsNothing()));

    public static final Block UNLIT_SOUL_LANTERN = registerBlockNoItem("unlit_soul_lantern",
            new UnlitLanternBlock(FabricBlockSettings.copyOf(Blocks.SOUL_LANTERN).luminance(state -> 0).dropsNothing()));


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(MasksNGlory.MOD_ID, name), block);
    }

    private static Block registerBlockNoItem(String name, Block block) {
        return Registry.register(Registries.BLOCK, new Identifier(MasksNGlory.MOD_ID, name), block);
    }

    public static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(MasksNGlory.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        MasksNGlory.LOGGER.info("Registering ModBlocks for " + MasksNGlory.MOD_ID);
    }
}
