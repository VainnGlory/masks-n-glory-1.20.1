package net.vainnglory.masksnglory.entity.custom;

import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.vainnglory.masksnglory.entity.client.ArmorStandThingModel;

public class ArmorStandThingRenderer extends GeoEntityRenderer<ArmorStandThingEntity> {

    public ArmorStandThingRenderer(EntityRendererFactory.Context context) {
        super(context, new ArmorStandThingModel());
    }
}