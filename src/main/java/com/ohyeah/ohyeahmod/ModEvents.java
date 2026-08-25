package com.ohyeah.ohyeahmod;

import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementTracker;
import com.ohyeah.ohyeahmod.entity.logic.SleepWakeGameplayCoordinator;
import com.ohyeah.ohyeahmod.registry.ModItems;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.TiansuluoPinkScarfEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
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

    @SubscribeEvent
    public static void onCampfireCookingStart(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getLevel().getBlockEntity(event.getPos()) instanceof CampfireBlockEntity campfire)
                || !CampfireBlock.isLitCampfire(event.getLevel().getBlockState(event.getPos()))
                || !isLuanluan(event.getItemStack())
                || campfire.getCookableRecipe(event.getItemStack()).isEmpty()) {
            return;
        }
        ModAdvancementTracker.award(player, ModAdvancementIds.SMOKE_LUANLUAN);
    }

    private static boolean isLuanluan(net.minecraft.world.item.ItemStack stack) {
        return stack.is(ModItems.TIANSULUO_PINK_SCARF_EGG.get())
                || stack.is(ModItems.TIANSULUO_BATTLE_FACE_EGG.get());
    }
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.getVehicle() instanceof TiansuluoPinkScarfEntity pinkScarf) {
            pinkScarf.tryStartRiderBurst(player, event.getTarget());
        }
    }
}
