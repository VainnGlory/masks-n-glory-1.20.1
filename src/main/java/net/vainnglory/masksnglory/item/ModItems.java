package net.vainnglory.masksnglory.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.item.custom.*;


public class ModItems {
    public static final Item ESHARD = registerItem("eshard", new Item(new FabricItemSettings()));
    public static final Item DSHARD = registerItem("dshard", new Item(new FabricItemSettings()));
    public static final Item OSHARD = registerItem("oshard", new Item(new FabricItemSettings()));
    public static final Item PSHARD = registerItem("pshard", new Item(new FabricItemSettings()));
    public static final Item MASKS = registerItem("masks", new Item(new FabricItemSettings()));
    public static final Item HMASKS = registerItem("hmasks", new Item(new FabricItemSettings()));
    public static final Item GMASKS = registerItem("gmasks", new Item(new FabricItemSettings()));
    public static final Item SMASKS = registerItem("smasks", new Item(new FabricItemSettings()));
    public static final Item KMASKS = registerItem("kmasks", new Item(new FabricItemSettings()));
    public static final Item EMASKS = registerItem("emasks", new Item(new FabricItemSettings()));
    public static final Item NMASKS = registerItem("nmasks", new Item(new FabricItemSettings()));
    public static final Item RUSTED = registerItem("rust", new Item(new FabricItemSettings()));
    public static final Item GLORIOUS = registerItem("void", new Item(new FabricItemSettings()));
    public static final Item BONE = registerItem("bone", new Item(new FabricItemSettings()));
    public static final Item RAWPALEINGOT = registerItem("rawpale", new Item(new FabricItemSettings()));
    public static final Item GILDEDINGOT = registerItem("gildedingot", new Item(new FabricItemSettings()));
    public static final Item ECHODUST = registerItem("echodust", new Item(new FabricItemSettings()));
    public static final Item GOLDENSCRAP = registerItem("goldenscrap", new GoldenScrapItem(new FabricItemSettings()));

    public static final Item EGO_MASK = registerItem("ego_mask",
        new ModArmorItem(ModArmorMaterials.ESHARD, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item DMAN_MASK = registerItem("d_man_mask",
            new ModArmorItem(ModArmorMaterials.DSHARD, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item BLANK_MASK = registerItem("blank_mask",
            new ModArmorItem(ModArmorMaterials.MASKS, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item HAPPY_MASK = registerItem("happy_mask",
            new ModArmorItem(ModArmorMaterials.HMASKS, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item GRIN_MASK = registerItem("grin_mask",
            new ModArmorItem(ModArmorMaterials.GMASKS, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item SAD_MASK = registerItem("sad_mask",
            new ModArmorItem(ModArmorMaterials.SMASKS, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item KNIGHT_MASK = registerItem("knight_mask",
            new ModArmorItem(ModArmorMaterials.KMASKS, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item EYE_MASK = registerItem("eye_mask",
            new ModArmorItem(ModArmorMaterials.EMASKS, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item NULL_MASK = registerItem("null_mask",
            new ModArmorItem(ModArmorMaterials.NMASKS, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item OJI_MASK = registerItem("oji_mask",
            new ModArmorItem(ModArmorMaterials.OSHARD, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static final Item PIKO_MASK = registerItem("piko_mask",
            new ModArmorItem(ModArmorMaterials.PSHARD, ArmorItem.Type.HELMET, new FabricItemSettings()));

    public static final Item RUSTED_SWORD = registerItem("rusted_sword",
            new RustedSwordItem(ModToolMaterial.RUSTED, 3 , -2.2f, new FabricItemSettings()));

    public static final Item MAELSTROM = registerItem("maelstrom",
            new MaelstromItem(ModToolMaterial.GLORIOUS, 5 , -2f, new FabricItemSettings()));

    public static final Item GOLDEN_PAN = registerItem("golden_pan",
            new GoldenPanItem(ModToolMaterial.RUSTED,1, -1.8f, new FabricItemSettings()));

    public static final Item PALE_SWORD = registerItem("pale_sword",
            new PaleSwordItem(ModToolMaterial.RUSTED, 4 , -2.8f, new FabricItemSettings()));


    public static final Item PALE_TEMPLATE = registerItem("pale_template", new Item(new FabricItemSettings()));


    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.add(ESHARD);
        entries.add(DSHARD);
        entries.add(RUSTED);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(MasksNGlory.MOD_ID, name), item);
    }

    public static void registerModItems() {
        MasksNGlory.LOGGER.info("Registering Mod Items for " + MasksNGlory.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientItemGroup);
    }
}
