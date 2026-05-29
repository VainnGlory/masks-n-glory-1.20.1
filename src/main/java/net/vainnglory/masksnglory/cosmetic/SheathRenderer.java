package net.vainnglory.masksnglory.cosmetic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@Environment(EnvType.CLIENT)
public class SheathRenderer extends GeoArmorRenderer<SheathItem> {

    public SheathRenderer() {
        super(new SheathModel());
    }

    @Override
    public RenderLayer getRenderType(SheathItem animatable, Identifier texture,
                                     @Nullable VertexConsumerProvider bufferSource, float partialTick) {
        return RenderLayer.getEntityCutout(texture);
    }
}
