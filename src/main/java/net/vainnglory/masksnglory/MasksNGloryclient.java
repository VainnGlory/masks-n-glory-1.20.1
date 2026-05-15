package net.vainnglory.masksnglory;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.entity.RavagerEntityRenderer;
import net.vainnglory.masksnglory.entity.client.MaelstromModel;
import net.vainnglory.masksnglory.entity.client.ModModelLayers;
import net.vainnglory.masksnglory.entity.custom.MaelstromEntityRenderer;
import net.vainnglory.masksnglory.entity.custom.ModEntityTypes;
import net.vainnglory.masksnglory.entity.custom.PaleSteelCoinRenderer;
import net.vainnglory.masksnglory.util.FlashEffectPacket;
import net.vainnglory.masksnglory.util.FlashOverlayRenderer;
import net.vainnglory.masksnglory.util.GreaseEffectPacket;
import net.vainnglory.masksnglory.util.ModKeybindings;

public class MasksNGloryclient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.MAELSTROME, MaelstromModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntityTypes.MAELSTROM_ENTITY_ENTITY_TYPE, MaelstromEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.PALE_STEEL_COIN_ENTITY_TYPE, PaleSteelCoinRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SOUL_RAVAGER_TYPE, RavagerEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SOUL_PROJECTILE_TYPE, EmptyEntityRenderer::new);

        ModKeybindings.register();

        FlashEffectPacket.registerClientReceiver();

        FlashOverlayRenderer.register();

        GreaseEffectPacket.registerClientReceiver();

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_TORCH, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_SOUL_TORCH, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_WALL_TORCH, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_SOUL_WALL_TORCH, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_LANTERN, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_SOUL_LANTERN, RenderLayer.getCutout());
    }
}

