package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;

/** 按单一战斗规则注册战颜的原生 Goal 组合。 */
public final class BattleFaceGoalRegistrar {
    public void registerGoals(TiansuluoBattleFaceEntity entity) {
        entity.goalSelector.addGoal(0, new FloatGoal(entity));
        // 反击 Goal 高于坐下，战斗时临时站起；结束后保留的坐下命令会让它自动坐回。
        entity.goalSelector.addGoal(1, new BattleFacePounceAttack(entity));
        entity.goalSelector.addGoal(2, new SitWhenOrderedToGoal(entity));
        entity.goalSelector.addGoal(3, new BreedGoal(entity, 1.1D));
        entity.goalSelector.addGoal(4, new TemptGoal(entity, 1.1D, entity::isFood, false));
        entity.goalSelector.addGoal(5, new FollowOwnerGoal(entity, 1.1D, 10.0F, 2.0F));
        entity.goalSelector.addGoal(7, new BattleFaceLayEggGoal(entity, BattleFaceProfile.EGG_LAYING_TICKS));
        entity.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(entity, 1.0D));
        entity.goalSelector.addGoal(9, new LookAtPlayerGoal(entity, Player.class, 6.0F));
        entity.goalSelector.addGoal(10, new RandomLookAroundGoal(entity));

        // 反击控制器和主人目标 Goal 只提供唯一目标，不叠加普通近战攻击逻辑。
        entity.targetSelector.addGoal(1, new BattleFaceOwnerHurtTargetGoal(entity));
    }
}
