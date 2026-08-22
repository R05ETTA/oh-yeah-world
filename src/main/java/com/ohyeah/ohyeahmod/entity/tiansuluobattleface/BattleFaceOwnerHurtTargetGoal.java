package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;

/** 将主人最近直接攻击的目标交给战颜自己的扑击反击链。 */
public final class BattleFaceOwnerHurtTargetGoal extends OwnerHurtTargetGoal {
    private final TiansuluoBattleFaceEntity battleFace;

    public BattleFaceOwnerHurtTargetGoal(TiansuluoBattleFaceEntity battleFace) {
        super(battleFace);
        this.battleFace = battleFace;
    }

    @Override
    public boolean canUse() {
        return this.battleFace.state().getRetaliationTicksRemaining() <= 0 && super.canUse();
    }

    @Override
    public void start() {
        super.start();
        LivingEntity target = this.battleFace.getTarget();
        if (target != null) {
            this.battleFace.beginOwnerRetaliation(target);
        }
    }
}
