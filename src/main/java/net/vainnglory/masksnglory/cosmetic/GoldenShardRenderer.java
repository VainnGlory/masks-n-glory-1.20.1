package net.vainnglory.masksnglory.cosmetic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@Environment(EnvType.CLIENT)
public class GoldenShardRenderer extends GeoArmorRenderer<GoldenShardItem> {

    public GoldenShardRenderer() {
        super(new GoldenShardModel());
    }

    @Override
    public RenderLayer getRenderType(GoldenShardItem animatable, Identifier texture,
                                     @Nullable VertexConsumerProvider bufferSource, float partialTick) {
        return RenderLayer.getEntityCutout(texture);
    }
}