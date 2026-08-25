package com.ohyeah.ohyeahmod;

import com.mojang.logging.LogUtils;
import com.ohyeah.ohyeahmod.registry.ModBlocks;
import com.ohyeah.ohyeahmod.registry.ModBlockEntities;
import com.ohyeah.ohyeahmod.registry.ModCreativeModeTabs;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import com.ohyeah.ohyeahmod.registry.ModItems;
import com.ohyeah.ohyeahmod.registry.ModSoundEvents;
import com.ohyeah.ohyeahmod.worldgen.ModEntityBiomeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(OhYeah.MODID)
public class OhYeah {
    public static final String MODID = "ohyeah";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OhYeah(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModSoundEvents.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        LOGGER.info("已初始化 {} 模组核心", MODID);
        LOGGER.info("自然生成方案：{}", ModEntityBiomeModifiers.summary());
    }
}
