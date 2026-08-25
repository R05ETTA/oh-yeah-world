package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;

/**
 * 基于原生 RangedAttackGoal 的受击反击目标。
 *
 * <p>目标选择、移动、视线判断和发射节奏交给原生 Goal；粉围巾实体只提供
 * performRangedAttack 的投射物实现和一个有限的受击记忆窗口。</p>
 */
public final class PinkScarfRetaliationGoal extends RangedAttackGoal {
    private final TiansuluoPinkScarfEntity pinkScarf;

    public PinkScarfRetaliationGoal(TiansuluoPinkScarfEntity pinkScarf, double speedModifier) {
        super(
                pinkScarf,
                speedModifier,
                PinkScarfProfile.BURST_INTERVAL_TICKS,
                PinkScarfProfile.RETALIATION_RANGE
        );
        this.pinkScarf = pinkScarf;
    }

    @Override
    public boolean canUse() {
        return !this.pinkScarf.isRiddenByOwner()
                && this.pinkScarf.isRetaliating()
                && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.pinkScarf.getTarget();
        return !this.pinkScarf.isRiddenByOwner()
                && this.pinkScarf.isRetaliating()
                && target != null
                && target.isAlive()
                && this.pinkScarf.distanceToSqr(target)
                <= (double) PinkScarfProfile.RETALIATION_RANGE * PinkScarfProfile.RETALIATION_RANGE
                && super.canContinueToUse();
    }

    @Override
    public void tick() {
        if (this.pinkScarf.isRetaliationDeclaring()) {
            LivingEntity target = this.pinkScarf.getTarget();
            if (target != null) {
                this.pinkScarf.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            this.pinkScarf.getNavigation().stop();
            return;
        }
        super.tick();
    }

    @Override
    public void stop() {
        super.stop();
        if (this.pinkScarf.isRetaliating()) {
            this.pinkScarf.finishRetaliation();
        }
    }
}
