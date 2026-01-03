package net.vainnglory.masksnglory.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.item.ArmorItem;
import net.vainnglory.masksnglory.item.ModItems;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {

    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

        itemModelGenerator.registerArmor(((ArmorItem) ModItems.PALE_HELMET));
        itemModelGenerator.registerArmor(((ArmorItem) ModItems.PALE_CHESTPLATE));
        itemModelGenerator.registerArmor(((ArmorItem) ModItems.PALE_LEGGINGS));
        itemModelGenerator.registerArmor(((ArmorItem) ModItems.PALE_BOOTS));

    }
}
