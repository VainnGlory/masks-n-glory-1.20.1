package net.vainnglory.masksnglory.cosmetic;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GoldenShardModel extends GeoModel<GoldenShardItem> {

    @Override
    public Identifier getModelResource(GoldenShardItem animatable) {
        return new Identifier("masks-n-glory", "geo/goldenshard.geo.json");
    }

    @Override
    public Identifier getTextureResource(GoldenShardItem animatable) {
        return new Identifier("masks-n-glory", "textures/item/goldenshard.png");
    }

    @Override
    public Identifier getAnimationResource(GoldenShardItem animatable) {
        return null;
    }
}
