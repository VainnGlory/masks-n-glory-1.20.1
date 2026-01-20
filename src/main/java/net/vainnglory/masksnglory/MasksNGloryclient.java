package net.vainnglory.masksnglory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.vainnglory.masksnglory.entity.client.MaelstromModel;
import net.vainnglory.masksnglory.entity.client.ModModelLayers;
import net.vainnglory.masksnglory.entity.custom.MaelstromEntityRenderer;
import net.vainnglory.masksnglory.entity.custom.ModEntityTypes;

public class MasksNGloryclient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.MAELSTROME, MaelstromModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntityTypes.MAELSTROM_ENTITY_ENTITY_TYPE, MaelstromEntityRenderer::new);

    }
    }

