package com.ohyeah.ohyeahmod.entity.suxia;

import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.player.Player;

/** 素虾只保留水生移动、逃跑和最爱食物诱食三类必要 Goal。 */
public final class SuxiaGoalRegistrar {
    public void registerGoals(SuxiaEntity entity) {
        entity.goalSelector.addGoal(0, new FloatGoal(entity));
        entity.goalSelector.addGoal(1, new TryFindWaterGoal(entity));
        entity.goalSelector.addGoal(2, new PanicGoal(entity, 1.25D));
        entity.goalSelector.addGoal(3, new TemptGoal(entity, 1.1D, entity::isFavoriteFood, false));
        entity.goalSelector.addGoal(4, new RandomSwimmingGoal(entity, 1.0D, 80));
        entity.goalSelector.addGoal(5, new LookAtPlayerGoal(entity, Player.class, 6.0F));
        entity.goalSelector.addGoal(6, new RandomLookAroundGoal(entity));
    }
}
