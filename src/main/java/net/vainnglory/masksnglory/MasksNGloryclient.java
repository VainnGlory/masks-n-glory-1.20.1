package net.vainnglory.masksnglory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.vainnglory.masksnglory.entity.ModEntities;
import net.vainnglory.masksnglory.entity.client.MaelstromModel;
import net.vainnglory.masksnglory.entity.client.MaelstromRenderer;
import net.vainnglory.masksnglory.entity.client.ModModelLayers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasksNGloryclient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(ModEntities.MAELSTROME, MaelstromRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.MAELSTROME, MaelstromModel::getTexturedModelData);

    }
}
