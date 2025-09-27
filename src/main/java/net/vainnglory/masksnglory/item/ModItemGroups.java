package net.vainnglory.masksnglory.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;

public class ModItemGroups {
    public static final ItemGroup SHARDS_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(MasksNGlory.MOD_ID, "shards"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.shards"))
                    .icon(() -> new ItemStack(ModItems.ESHARD)).entries((displayContext, entries) -> {
                        entries.add(ModItems.ESHARD);
                        entries.add(ModItems.DSHARD);

                        entries.add(ModItems.EGO_MASK);
                        entries.add(ModItems.DMAN_MASK);
                    }).build());

    public static void registerItemGroups() {
        MasksNGlory.LOGGER.info("Registering Item Groups for " +MasksNGlory.MOD_ID);
    }
}
