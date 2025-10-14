package net.vainnglory.masksnglory.entity.client;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;
import net.vainnglory.masksnglory.MasksNGloryclient;
import net.vainnglory.masksnglory.entity.custom.MaelstromEntity;


public class MaelstromRenderer extends ProjectileEntityRenderer<MaelstromEntity> {
    private static final Identifier TEXTURE = new Identifier(MasksNGlory.MOD_ID, "textures/entity/maelstrom_model.png");
    public MaelstromRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(MaelstromEntity entity) {
        return TEXTURE;
    }
}
