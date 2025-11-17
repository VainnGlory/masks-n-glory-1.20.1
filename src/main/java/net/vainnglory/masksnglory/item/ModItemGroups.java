package net.vainnglory.masksnglory.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.block.ModBlocks;

public class ModItemGroups {
    public static final ItemGroup SHARDS_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(MasksNGlory.MOD_ID, "shards"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.shards"))
                    .icon(() -> new ItemStack(ModItems.RUSTED)).entries((displayContext, entries) -> {
                        entries.add(ModItems.ESHARD);
                        entries.add(ModItems.DSHARD);
                        entries.add(ModItems.OSHARD);
                        entries.add(ModItems.PSHARD);
                        entries.add(ModItems.TSHARD);
                        entries.add(ModItems.HSSHARD);
                        entries.add(ModItems.DOSHARD);
                        entries.add(ModItems.CSHARD);
                        entries.add(ModItems.RUSTED);
                        entries.add(ModItems.RAWPALEINGOT);
                        entries.add(ModItems.GILDEDINGOT);
                        entries.add(ModItems.BONE);
                        entries.add(ModItems.ECHODUST);
                        entries.add(ModItems.ECHODUSTSUGAR);
                        entries.add(ModItems.GOLDENSCRAP);

                        entries.add(ModItems.EGO_MASK);
                        entries.add(ModItems.DMAN_MASK);
                        entries.add(ModItems.OJI_MASK);
                        entries.add(ModItems.PIKO_MASK);
                        entries.add(ModItems.TOG_MASK);
                        entries.add(ModItems.HS_MASK);
                        entries.add(ModItems.DOG_MASK);
                        entries.add(ModItems.CORV_MASK);
                        entries.add(ModItems.BLANK_MASK);
                        entries.add(ModItems.GRIN_MASK);
                        entries.add(ModItems.SAD_MASK);
                        entries.add(ModItems.HAPPY_MASK);
                        entries.add(ModItems.KNIGHT_MASK);
                        entries.add(ModItems.EYE_MASK);
                        entries.add(ModItems.NULL_MASK);

                        entries.add(ModItems.RUSTED_SWORD);
                        entries.add(ModItems.MAELSTROM);
                        entries.add(ModItems.GOLDEN_PAN);
                        entries.add(ModItems.PALE_SWORD);

                        entries.add(ModItems.PALE_TEMPLATE);

                        entries.add(ModItems.GLORIOUS);

                        entries.add(ModBlocks.PALE_STEEL_BLOCK);
                        entries.add(ModBlocks.PALE_STEEL_TILES);
                        entries.add(ModBlocks.CRIMPED_CHISELED_PALE_STEEL_BLOCK);
                        entries.add(ModBlocks.CHISELED_PALE_STEEL_BLOCK);

                        entries.add(ModItems.GILDED_SOUP);
                        entries.add(ModItems.ECHO_CARAMEL);
                        entries.add(ModItems.CHOGLOWBERRY);
                        entries.add(ModItems.GILDED_HEART);


                    }).build());

    public static void registerItemGroups() {
        MasksNGlory.LOGGER.info("Registering Item Groups for " +MasksNGlory.MOD_ID);
    }
}
