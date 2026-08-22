package com.ohyeah.ohyeahmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.client.model.TiansuluoPinkScarfEntityModel;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.TiansuluoPinkScarfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** 粉围巾模型的原生 MobRenderer 缩放/朝向实现。 */
public class TiansuluoPinkScarfEntityRenderer extends MobRenderer<TiansuluoPinkScarfEntity, TiansuluoPinkScarfEntityModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            OhYeah.MODID,
            "textures/entity/tiansuluo_pink_scarf.png"
    );

    public TiansuluoPinkScarfEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new TiansuluoPinkScarfEntityModel(context.bakeLayer(TiansuluoPinkScarfEntityModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(TiansuluoPinkScarfEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(TiansuluoPinkScarfEntity entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        if (entity.isBaby()) {
            poseStack.scale(0.55F, 0.55F, 0.55F);
        }
    }
}
