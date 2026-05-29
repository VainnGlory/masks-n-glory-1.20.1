package net.vainnglory.masksnglory.cosmetic;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class SheathModel extends GeoModel<SheathItem> {

    @Override
    public Identifier getModelResource(SheathItem animatable) {
        return new Identifier("masks-n-glory", "geo/sheath.geo.json");
    }

    @Override
    public Identifier getTextureResource(SheathItem animatable) {
        return new Identifier("masks-n-glory", "textures/item/sheath.png");
    }

    @Override
    public Identifier getAnimationResource(SheathItem animatable) {
        return null;
    }
}
