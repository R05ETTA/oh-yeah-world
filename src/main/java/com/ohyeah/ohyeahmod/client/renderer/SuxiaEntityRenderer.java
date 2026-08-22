package com.ohyeah.ohyeahmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.client.model.SuxiaEntityModel;
import com.ohyeah.ohyeahmod.entity.suxia.SuxiaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SuxiaEntityRenderer extends MobRenderer<SuxiaEntity, SuxiaEntityModel<SuxiaEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, "textures/entity/suxia.png");

    public SuxiaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SuxiaEntityModel<>(context.bakeLayer(SuxiaEntityModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(SuxiaEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(
            SuxiaEntity entity,
            PoseStack poseStack,
            float bob,
            float yBodyRot,
            float partialTick,
            float scale
    ) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        float renderScale = 0.5F;
        poseStack.scale(renderScale, renderScale, renderScale);
    }
}
