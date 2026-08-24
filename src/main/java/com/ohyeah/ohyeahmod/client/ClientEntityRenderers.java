package com.ohyeah.ohyeahmod.client;

import com.ohyeah.ohyeahmod.client.model.SuxiaEntityModel;
import com.ohyeah.ohyeahmod.client.model.TiansuluoBattleFaceEntityModel;
import com.ohyeah.ohyeahmod.client.model.TiansuluoPinkScarfEntityModel;
import com.ohyeah.ohyeahmod.client.renderer.SuxiaEntityRenderer;
import com.ohyeah.ohyeahmod.client.renderer.TiansuluoBattleFaceEntityRenderer;
import com.ohyeah.ohyeahmod.client.renderer.TiansuluoPinkScarfEntityRenderer;
import com.ohyeah.ohyeahmod.entity.suxia.SuxiaLuanluanProjectileEntity;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.PinkScarfProjectileEntity;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 客户端实体渲染器注册类。
 * <p>
 * 已移除废弃的 @EventBusSubscriber 注解。
 * 事件现在通过 OhYeahClient 构造函数中的 modEventBus 手动注册，这是 NeoForge 1.21+ 的最佳实践。
 */
public final class ClientEntityRenderers {

    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SuxiaEntityModel.LAYER_LOCATION, SuxiaEntityModel::createBodyLayer);        
        event.registerLayerDefinition(TiansuluoPinkScarfEntityModel.LAYER_LOCATION, TiansuluoPinkScarfEntityModel::createBodyLayer);
        event.registerLayerDefinition(TiansuluoBattleFaceEntityModel.LAYER_LOCATION, TiansuluoBattleFaceEntityModel::createBodyLayer);
    }

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.TIANSULUO_PINK_SCARF.get(), TiansuluoPinkScarfEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TIANSULUO_BATTLE_FACE.get(), TiansuluoBattleFaceEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TIANSULUO_PINK_SCARF_PROJECTILE.get(), context -> new ThrownItemRenderer<PinkScarfProjectileEntity>(context, 1.0F, true));
        event.registerEntityRenderer(ModEntityTypes.SUXIA_LUANLUAN_PROJECTILE.get(), context -> new ThrownItemRenderer<SuxiaLuanluanProjectileEntity>(context, 1.0F, true));
        event.registerEntityRenderer(ModEntityTypes.SUXIA.get(), SuxiaEntityRenderer::new);
    }
}
