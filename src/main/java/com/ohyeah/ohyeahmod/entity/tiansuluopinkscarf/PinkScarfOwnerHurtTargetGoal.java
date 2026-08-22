package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;

/** 将主人最近直接攻击的目标交给粉围巾自己的远程反击链。 */
public final class PinkScarfOwnerHurtTargetGoal extends OwnerHurtTargetGoal {
    private final TiansuluoPinkScarfEntity pinkScarf;

    public PinkScarfOwnerHurtTargetGoal(TiansuluoPinkScarfEntity pinkScarf) {
        super(pinkScarf);
        this.pinkScarf = pinkScarf;
    }

    @Override
    public boolean canUse() {
        return !this.pinkScarf.isRetaliating() && super.canUse();
    }

    @Override
    public void start() {
        super.start();
        LivingEntity target = this.pinkScarf.getTarget();
        if (target != null) {
            this.pinkScarf.beginRetaliation(target);
        }
    }
}
