package net.vainnglory.masksnglory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.RavagerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.vainnglory.masksnglory.block.ModBlocks;
import net.vainnglory.masksnglory.cosmetic.GoldenShardLayer;
import net.vainnglory.masksnglory.enchantments.ModEnchantments;
import net.vainnglory.masksnglory.entity.client.MaelstromModel;
import net.vainnglory.masksnglory.entity.client.ModModelLayers;
import net.vainnglory.masksnglory.entity.custom.MaelstromEntityRenderer;
import net.vainnglory.masksnglory.entity.custom.ModEntityTypes;
import net.vainnglory.masksnglory.entity.custom.PaleSteelCoinRenderer;
import net.vainnglory.masksnglory.entity.custom.ShearProjectileRenderer;
import net.vainnglory.masksnglory.item.ModItems;
import net.vainnglory.masksnglory.item.custom.PaleWarPickaxeItem;
import net.vainnglory.masksnglory.util.FlashEffectPacket;
import net.vainnglory.masksnglory.util.FlashOverlayRenderer;
import net.vainnglory.masksnglory.util.ModKeybindings;

public class MasksNGloryclient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_TORCH, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_SOUL_TORCH, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_WALL_TORCH, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_SOUL_WALL_TORCH, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_LANTERN, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNLIT_SOUL_LANTERN, RenderLayer.getCutout());

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.MAELSTROME, MaelstromModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntityTypes.MAELSTROM_ENTITY_ENTITY_TYPE, MaelstromEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.PALE_STEEL_COIN_ENTITY_TYPE, PaleSteelCoinRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SOUL_RAVAGER_TYPE, RavagerEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SOUL_PROJECTILE_TYPE, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SHEAR_PROJECTILE_TYPE, ShearProjectileRenderer::new);

        ModelPredicateProviderRegistry.register(ModItems.PALE_WAR_PICKAXE, new Identifier("artillery"),
                (stack, world, entity, seed) ->
                        EnchantmentHelper.getLevel(ModEnchantments.ARTILLERY, stack) > 0 ? 1.0f : 0.0f
        );

        ModelPredicateProviderRegistry.register(ModItems.PALE_WAR_PICKAXE, new Identifier("pulling"),
                (stack, world, entity, seed) -> {
                    if (entity == null) return 0.0f;
                    if (EnchantmentHelper.getLevel(ModEnchantments.ARTILLERY, stack) <= 0) return 0.0f;
                    if (PaleWarPickaxeItem.isLoaded(stack)) return 0.0f;
                    return entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0f : 0.0f;
                });

        ModelPredicateProviderRegistry.register(ModItems.PALE_WAR_PICKAXE, new Identifier("pull"),
                (stack, world, entity, seed) -> {
                    if (entity == null || entity.getActiveItem() != stack) return 0.0f;
                    if (PaleWarPickaxeItem.isLoaded(stack)) return 0.0f;
                    return MathHelper.clamp(
                            (stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / (float) PaleWarPickaxeItem.PULL_TIME,
                            0f, 1f
                    );
                });

        ModelPredicateProviderRegistry.register(ModItems.PALE_WAR_PICKAXE, new Identifier("charged"),
                (stack, world, entity, seed) ->
                        EnchantmentHelper.getLevel(ModEnchantments.ARTILLERY, stack) > 0
                                && PaleWarPickaxeItem.isLoaded(stack) ? 1.0f : 0.0f
        );

        ModKeybindings.register();
        FlashEffectPacket.registerClientReceiver();
        FlashOverlayRenderer.register();

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, registrationHelper, context) -> {
            if (entityType == EntityType.PLAYER) {
                registrationHelper.register(new GoldenShardLayer(
                        (FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>) renderer
                ));
            }
        });
    }
}

