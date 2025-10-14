package net.vainnglory.masksnglory.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.vainnglory.masksnglory.entity.custom.MaelstromEntity;

public class MaelstromModel<T extends MaelstromEntity> extends SinglePartEntityModel<T> {
	private final ModelPart Maelstrom;

	public MaelstromModel(ModelPart root) {
		this.Maelstrom = root.getChild("maelstrom");
	}
	public static TexturedModelData getTexturedModelData() {

		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData Maelstrom = modelPartData.addChild("maelstrom", ModelPartBuilder.create().uv(0, 0).cuboid(-0.7F, 5.8F, -0.7F, 1.4F, 1.4F, 1.4F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-0.5F, 1.0F, -0.5F, 1.0F, 5.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-1.5F, 0.5F, -2.5F, 3.0F, 0.5F, 5.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-0.25F, -24.0F, -2.5F, 0.5F, 24.5F, 5.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-0.7F, 0.8F, -0.7F, 1.4F, 1.4F, 1.4F, new Dilation(0.0F)), ModelTransform.of(0.0F, 16.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		ModelPartData Mal11_r1 = Maelstrom.addChild("Mal11_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-0.15F, -1.0F, -1.0F, 0.3F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -0.7678F, -5.0607F, 0.7854F, 0.0F, 0.0F));

		ModelPartData Mal10_r1 = Maelstrom.addChild("Mal10_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-0.15F, -1.0F, -1.0F, 0.3F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -0.7678F, 4.9749F, -0.7854F, 0.0F, 0.0F));

		ModelPartData Mal9_r1 = Maelstrom.addChild("Mal9_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-0.15F, 0.6036F, -0.25F, 0.3F, 1.0F, 0.5F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-0.15F, -0.3964F, -1.25F, 0.3F, 1.0F, 1.5F, new Dilation(0.0F)), ModelTransform.of(0.0F, -0.3107F, -3.5429F, 0.7854F, 0.0F, 0.0F));

		ModelPartData Mal7_r1 = Maelstrom.addChild("Mal7_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-0.5F, 0.0F, -1.0F, 0.3F, 1.0F, 0.5F, new Dilation(0.0F)), ModelTransform.of(0.35F, 0.6464F, 3.5607F, -0.7854F, 0.0F, 0.0F));

		ModelPartData Mal6_r1 = Maelstrom.addChild("Mal6_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-0.15F, -0.5F, -0.75F, 0.3F, 1.0F, 1.5F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.1161F, 3.7374F, -0.7854F, 0.0F, 0.0F));
		return TexturedModelData.of(modelData, 24, 39);
	}
	@Override
	public void setAngles(MaelstromEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		Maelstrom.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}

    @Override
    public ModelPart getPart() {
        return Maelstrom;
    }
}