package com.ohyeah.ohyeahmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.client.model.TiansuluoBattleFaceEntityModel;
import com.ohyeah.ohyeahmod.entity.tiansuluobattleface.BattleFaceProfile;
import com.ohyeah.ohyeahmod.entity.tiansuluobattleface.TiansuluoBattleFaceEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TiansuluoBattleFaceEntityRenderer extends MobRenderer<TiansuluoBattleFaceEntity, TiansuluoBattleFaceEntityModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, "textures/entity/tiansuluo_battle_face.png");

    public TiansuluoBattleFaceEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new TiansuluoBattleFaceEntityModel(context.bakeLayer(TiansuluoBattleFaceEntityModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(TiansuluoBattleFaceEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(TiansuluoBattleFaceEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        float renderScale = 0.6F;
        if (entity.isBaby()) {
            renderScale *= BattleFaceProfile.BABY_SCALE_FACTOR;
        }
        poseStack.scale(renderScale, renderScale, renderScale);
    }
}
