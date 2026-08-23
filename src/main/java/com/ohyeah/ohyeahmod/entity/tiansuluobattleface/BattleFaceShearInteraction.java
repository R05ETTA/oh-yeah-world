package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 负责 Battle Face 的剪毛交互逻辑。
 */
public final class BattleFaceShearInteraction {
    public InteractionResult handleShear(TiansuluoBattleFaceEntity entity, Player player, InteractionHand hand, ItemStack stack) {
        if (!stack.is(Items.SHEARS) || entity.state().isSilenced(entity)) {
            return InteractionResult.PASS;
        }
        if (entity.level().isClientSide) {
            return InteractionResult.CONSUME;
        }
        entity.spawnAtLocation(Items.RED_WOOL);
        entity.level().playSound(null, entity, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
        entity.level().broadcastEntityEvent(entity, BattleFaceProfile.EVENT_SHEAR_REACT);
        entity.state().setSilenced(entity, true);
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancementTracker.award(serverPlayer, ModAdvancementIds.SHEAR);
        }
        return InteractionResult.SUCCESS;
    }
}
