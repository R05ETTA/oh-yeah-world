package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementTracker;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.EventHooks;
/**
 * 负责 Battle Face 的日常行为，包括喂食、驯服、求偶与繁殖结果处理。
 */
public final class BattleFaceBehavior {
    public InteractionResult handleMobInteract(TiansuluoBattleFaceEntity entity, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean favoriteFood = BattleFaceProfile.FOODS.isFavorite(stack);

        if (!entity.isFood(stack)) {
            return InteractionResult.PASS;
        }

        boolean isBaby = entity.isBaby();
        boolean isTamingAttempt = !isBaby && !entity.isTame() && favoriteFood;
        boolean isOwner = entity.isTame() && entity.isOwnedBy(player);
        boolean isHealing = isOwner && !isBaby && entity.getHealth() < entity.getMaxHealth();
        boolean isBreeding = isOwner
                && !isBaby
                && !entity.isInSittingPose()
                && !entity.state().hasCarriedEggBlock(entity)
                && entity.getHealth() >= entity.getMaxHealth()
                && entity.getAge() == 0
                && !entity.isInLove()
                && favoriteFood;

        /* 先判断服务端真正会接受的交互，避免无效喂食提前返回 CONSUME、解除禁声或播放特效。 */
        if (!isBaby && !isTamingAttempt && !isHealing && !isBreeding) {
            return InteractionResult.PASS;
        }
        if (entity.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        boolean wasSilenced = entity.state().isSilenced(entity);
        entity.consumeInteractionItem(player, hand, stack);
        entity.state().setSilenced(entity, false);
        if (wasSilenced && player instanceof ServerPlayer serverPlayer) {
            ModAdvancementTracker.award(serverPlayer, ModAdvancementIds.RESTORE_VOICE);
        }

        if (isBaby) {
            if (favoriteFood) {
                entity.setAge(0);
            } else {
                entity.ageUp(BattleFaceProfile.FOOD_GROWTH_STEP / 20, true);
            }
            entity.level().broadcastEntityEvent(
                    entity,
                    favoriteFood ? BattleFaceProfile.EVENT_EAT_FAVORITE : BattleFaceProfile.EVENT_EAT
            );
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTracker.award(serverPlayer, ModAdvancementIds.FEED_GROW);
            }
            return InteractionResult.SUCCESS;
        }

        if (isTamingAttempt) {
            if (entity.getRandom().nextInt(3) == 0 && !EventHooks.onAnimalTame(entity, player)) {
                entity.tame(player);
                if (player instanceof ServerPlayer serverPlayer) {
                    ModAdvancementTracker.award(serverPlayer, ModAdvancementIds.TAME_BATTLE_FACE);
                }
                entity.getNavigation().stop();
                entity.setTarget(null);
                entity.level().broadcastEntityEvent(entity, (byte) 7);
            } else {
                entity.level().broadcastEntityEvent(entity, (byte) 6);
            }
            entity.level().broadcastEntityEvent(
                    entity,
                    favoriteFood ? BattleFaceProfile.EVENT_EAT_FAVORITE : BattleFaceProfile.EVENT_EAT
            );
            return InteractionResult.SUCCESS;
        }

        if (isHealing) {
            entity.heal(BattleFaceProfile.FOOD_HEAL_AMOUNT * (favoriteFood ? 1.0F : 0.5F));
            entity.level().broadcastEntityEvent(
                    entity,
                    favoriteFood ? BattleFaceProfile.EVENT_EAT_FAVORITE : BattleFaceProfile.EVENT_EAT
            );
            return InteractionResult.SUCCESS;
        }

        if (isBreeding) {
            entity.setInLove(player);
            entity.level().broadcastEntityEvent(entity, BattleFaceProfile.EVENT_EAT_FAVORITE);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public void spawnChildFromBreeding(TiansuluoBattleFaceEntity entity, ServerLevel level, Animal mate) {
        if (!(mate instanceof TiansuluoBattleFaceEntity partner)
                || entity.state().hasCarriedEggBlock(entity)
                || partner.state().hasCarriedEggBlock(partner)) {
            return;
        }

        TiansuluoBattleFaceEntity parent = entity.getRandom().nextBoolean() ? entity : partner;
        parent.state().setHasCarriedEggBlock(parent, true);

        entity.setAge(BattleFaceProfile.PARENT_BREEDING_COOLDOWN_TICKS);
        partner.setAge(BattleFaceProfile.PARENT_BREEDING_COOLDOWN_TICKS);
        entity.resetLove();
        partner.resetLove();
        level.broadcastEntityEvent(entity, (byte) 18);
        level.broadcastEntityEvent(parent, BattleFaceProfile.EVENT_BREED_SUCCESS);

        ServerPlayer player = entity.getLoveCause();
        if (player == null) {
            player = partner.getLoveCause();
        }
        if (player != null) {
            ModAdvancementTracker.award(player, ModAdvancementIds.BREED_TIANSULUO);
            player.awardStat(Stats.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger(player, entity, partner, null);
            player.displayClientMessage(
                    Component.translatable(BattleFaceProfile.getCarriedMessageKey()),
                    false
            );
        }

        if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            level.addFreshEntity(new ExperienceOrb(
                    level,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    entity.getRandom().nextInt(7) + 1
            ));
        }
    }

    public boolean canMate(TiansuluoBattleFaceEntity entity, Animal other) {
        if (other == entity || !(other instanceof TiansuluoBattleFaceEntity partner)) {
            return false;
        }
        if (!entity.isTame() || !partner.isTame()) {
            return false;
        }
        if (entity.isBaby() || partner.isBaby()
                || entity.state().hasCarriedEggBlock(entity)
                || partner.state().hasCarriedEggBlock(partner)) {
            return false;
        }
        if (entity.isInSittingPose() || partner.isInSittingPose()) {
            return false;
        }
        return entity.isInLove() && partner.isInLove();
    }
}
