package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf;

import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;

/** 按原生 Goal 组合注册粉围巾的行为树。 */
public final class PinkScarfGoalRegistrar {
    public void registerGoals(TiansuluoPinkScarfEntity entity) {
        entity.goalSelector.addGoal(0, new FloatGoal(entity));
        /* 反击必须高于坐下、繁殖和寻路，避免受击后被其他 MOVE Goal 抢占。 */
        entity.goalSelector.addGoal(1, new PinkScarfRetaliationGoal(entity, PinkScarfProfile.RETALIATION_GOAL_SPEED));
        entity.goalSelector.addGoal(2, new SitWhenOrderedToGoal(entity));
        entity.goalSelector.addGoal(3, new PinkScarfBreedGoal(entity, 1.0D));
        entity.goalSelector.addGoal(4, new PinkScarfLayEggGoal(entity, PinkScarfProfile.EGG_LAYING_TICKS));
        entity.goalSelector.addGoal(5, new TemptGoal(
                entity,
                1.1D,
                stack -> entity.isFood(stack) && !entity.state().hasCarriedEggBlock(entity),
                false
        ));
        entity.goalSelector.addGoal(6, new FollowOwnerGoal(entity, 1.0D, 10.0F, 2.0F));
        entity.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(entity, 1.0D));
        entity.goalSelector.addGoal(8, new LookAtPlayerGoal(entity, Player.class, 6.0F));
        entity.goalSelector.addGoal(9, new RandomLookAroundGoal(entity));

        /* 只响应自身受击或主人直接攻击的目标，不自行寻找战斗目标。 */
        entity.targetSelector.addGoal(1, new PinkScarfHurtByTargetGoal(entity));
        entity.targetSelector.addGoal(2, new PinkScarfOwnerHurtTargetGoal(entity));
    }
}
