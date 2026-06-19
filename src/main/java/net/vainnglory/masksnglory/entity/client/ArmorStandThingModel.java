package net.vainnglory.masksnglory.entity.client;

import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.entity.custom.ArmorStandThingEntity;
import software.bernie.geckolib.model.GeoModel;

public class ArmorStandThingModel extends GeoModel<ArmorStandThingEntity> {

    @Override
    public Identifier getModelResource(ArmorStandThingEntity animatable) {
        return new Identifier(MasksNGlory.MOD_ID, "geo/entity/armor_stand_thing.geo.json");
    }

    @Override
    public Identifier getTextureResource(ArmorStandThingEntity animatable) {
        return new Identifier(MasksNGlory.MOD_ID, "textures/entity/armor_stand_thing.png");
    }

    @Override
    public Identifier getAnimationResource(ArmorStandThingEntity animatable) {
        return new Identifier(MasksNGlory.MOD_ID, "animations/armor_stand_thing.animation.json");
    }
}
