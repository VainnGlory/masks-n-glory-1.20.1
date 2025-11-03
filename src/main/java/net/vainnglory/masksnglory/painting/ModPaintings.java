package net.vainnglory.masksnglory.painting;

import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;

public class ModPaintings {
    public static final PaintingVariant HUSK = registerPainting("husk",new PaintingVariant(48,64));
    public static final PaintingVariant RATS = registerPainting("rats",new PaintingVariant(64,48));
    public static final PaintingVariant STOP = registerPainting("stop",new PaintingVariant(64,48));
    public static PaintingVariant registerPainting(String name, PaintingVariant paintingVariant){
        return Registry.register(Registries.PAINTING_VARIANT,new Identifier(MasksNGlory.MOD_ID,name),paintingVariant);
    }
    public static void registerPaintings(){
    }
}
