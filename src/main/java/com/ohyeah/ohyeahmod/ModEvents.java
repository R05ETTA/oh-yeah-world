package com.ohyeah.ohyeahmod;

import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementTracker;
import com.ohyeah.ohyeahmod.entity.logic.SleepWakeGameplayCoordinator;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

/**
 * 监听 Minecraft 游戏内部的事件 (Game Event Bus)。
 * 在 NeoForge 1.21+ 中，@EventBusSubscriber 默认监听游戏总线，无需且不应指定 bus 参数。
 */
@EventBusSubscriber(modid = OhYeah.MODID)
public final class ModEvents {
    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.updateLevel()) {
            player.getSleepingPos().ifPresent(pos -> {
                SleepWakeGameplayCoordinator.trySpawnAfterWake(player, pos);
            });
        }
    }

    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getSmelting().is(ModItems.XIAMI_HUHU.get())) {
            ModAdvancementTracker.award(player, ModAdvancementIds.SMOKE_LUANLUAN);
        }
    }

    @SubscribeEvent
    public static void onItemFinished(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getItem().is(ModItems.XIAMI_HUHU.get())) {
            ModAdvancementTracker.award(player, ModAdvancementIds.EAT_XIAMI_HUHU);
        }
    }
}
