package net.vainnglory.masksnglory.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.item.custom.ModArmorItem;

public class ModItems {
    public static final Item ESHARD = registerItem("eshard", new Item(new FabricItemSettings()));
    public static final Item DSHARD = registerItem("dshard", new Item(new FabricItemSettings()));

    public static final Item EGO_MASK = registerItem("ego_mask",
        new ModArmorItem(ModArmorMaterials.ESHARD, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item DMAN_MASK = registerItem("d_man_mask",
            new ModArmorItem(ModArmorMaterials.DSHARD, ArmorItem.Type.HELMET, new FabricItemSettings()));

    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.add(ESHARD);
        entries.add(DSHARD);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(MasksNGlory.MOD_ID, name), item);
    }

    public static void registerModItems() {
        MasksNGlory.LOGGER.info("Registering Mod Items for " + MasksNGlory.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientItemGroup);
    }
}
