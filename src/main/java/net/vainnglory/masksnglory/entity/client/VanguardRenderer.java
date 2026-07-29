package net.vainnglory.masksnglory.entity.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.entity.custom.VanguardEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanguardRenderer extends MobEntityRenderer<VanguardEntity, PlayerEntityModel<VanguardEntity>> {
    private static final Map<UUID, Identifier> SKIN_CACHE = new ConcurrentHashMap<>();
    private static final Set<UUID> REQUESTED = ConcurrentHashMap.newKeySet();

    public VanguardRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
        this.addFeature(new ArmorFeatureRenderer<>(this,
                new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)),
                new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addFeature(new HeldItemFeatureRenderer<>(this, context.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(VanguardEntity entity) {
        UUID skinUuid = entity.getSkinUuid().orElse(null);
        if (skinUuid == null) return DefaultSkinHelper.getTexture(entity.getUuid());

        Identifier cached = SKIN_CACHE.get(skinUuid);
        if (cached != null) return cached;

        if (REQUESTED.add(skinUuid)) {
            GameProfile profile = new GameProfile(skinUuid, "Vanguard");
            MinecraftClient.getInstance().getSkinProvider().loadSkin(profile, (type, id, texture) -> {
                if (type == MinecraftProfileTexture.Type.SKIN) {
                    SKIN_CACHE.put(skinUuid, id);
                }
            }, false);
        }
        return DefaultSkinHelper.getTexture(skinUuid);
    }
}
