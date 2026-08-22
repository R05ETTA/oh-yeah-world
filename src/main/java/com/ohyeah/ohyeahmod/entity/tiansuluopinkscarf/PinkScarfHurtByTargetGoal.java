package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

/** 只把直接攻击粉围巾的有效攻击者交给反击目标系统。 */
public final class PinkScarfHurtByTargetGoal extends HurtByTargetGoal {
    private final TiansuluoPinkScarfEntity pinkScarf;

    public PinkScarfHurtByTargetGoal(TiansuluoPinkScarfEntity pinkScarf) {
        super(pinkScarf);
        this.pinkScarf = pinkScarf;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        LivingEntity attacker = this.pinkScarf.getLastHurtByMob();
        return this.pinkScarf.canRetaliateAgainst(attacker);
    }
}
