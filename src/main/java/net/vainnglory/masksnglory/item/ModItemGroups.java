package net.vainnglory.masksnglory.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.item.custom.ModArmorItem;

public class ModItemGroups {
    public static final ItemGroup SHARDS_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(MasksNGlory.MOD_ID, "shards"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.shards"))
                    .icon(() -> new ItemStack(ModItems.RUSTED)).entries((displayContext, entries) -> {
                        entries.add(ModItems.ESHARD);
                        entries.add(ModItems.DSHARD);
                        entries.add(ModItems.OSHARD);
                        entries.add(ModItems.PSHARD);
                        entries.add(ModItems.RUSTED);
                        entries.add(ModItems.RAWPALEINGOT);
                        entries.add(ModItems.GILDEDINGOT);
                        entries.add(ModItems.ECHODUST);
                        entries.add(ModItems.GOLDENSCRAP);

                        entries.add(ModItems.EGO_MASK);
                        entries.add(ModItems.DMAN_MASK);
                        entries.add(ModItems.OJI_MASK);
                        entries.add(ModItems.PIKO_MASK);
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

                        entries.add(ModItems.PALE_TEMPLATE);

                        entries.add(ModItems.GLORIOUS);

                    }).build());

    public static void registerItemGroups() {
        MasksNGlory.LOGGER.info("Registering Item Groups for " +MasksNGlory.MOD_ID);
    }
}
