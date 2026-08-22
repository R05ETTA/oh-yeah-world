package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * 负责战颜反击窗口和唯一攻击目标的服务端状态推进。
 */
public final class BattleFaceRetaliationController {
    public void onHurt(TiansuluoBattleFaceEntity entity, boolean hurt, DamageSource source) {
        if (!hurt || entity.level().isClientSide || !(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        this.begin(entity, attacker);
    }

    public void onOwnerHurtTarget(TiansuluoBattleFaceEntity entity, LivingEntity target) {
        if (entity.level().isClientSide || target == null || !target.isAlive()) {
            return;
        }
        this.begin(entity, target);
    }

    private void begin(TiansuluoBattleFaceEntity entity, LivingEntity target) {
        BattleFaceState state = entity.state();
        state.setRetaliationTicksRemaining(BattleFaceProfile.RETALIATION_TOTAL_TICKS);
        state.setRetaliationDeclareTicksRemaining(BattleFaceProfile.RETALIATION_DECLARE_TICKS);
        state.setPounceCooldownTicks(0);
        entity.setTarget(target);
        entity.level().broadcastEntityEvent(entity, BattleFaceProfile.EVENT_ATTACK_DECLARE);
    }

    public void tick(TiansuluoBattleFaceEntity entity) {
        if (entity.level().isClientSide) return;

        BattleFaceState state = entity.state();
        if (state.getRetaliationTicksRemaining() <= 0) {
            entity.setTarget(null);
            return;
        }

        state.decrementRetaliationTicks();
        LivingEntity target = entity.getTarget();
        if (target == null
                || !target.isAlive()
                || entity.distanceToSqr(target) > BattleFaceProfile.RETALIATION_MAX_RANGE_SQUARED
                || state.getRetaliationTicksRemaining() <= 0) {
            this.finish(entity);
            return;
        }

        entity.getLookControl().setLookAt(target, 22.0F, 22.0F);
        if (state.getRetaliationDeclareTicksRemaining() > 0) {
            state.decrementRetaliationDeclareTicks();
        }
    }

    private void finish(TiansuluoBattleFaceEntity entity) {
        BattleFaceState state = entity.state();
        boolean active = state.getRetaliationTicksRemaining() > 0
                || state.getRetaliationDeclareTicksRemaining() > 0
                || entity.getTarget() != null;
        state.setRetaliationTicksRemaining(0);
        state.setRetaliationDeclareTicksRemaining(0);
        entity.setTarget(null);
        if (active) {
            entity.level().broadcastEntityEvent(entity, BattleFaceProfile.EVENT_ATTACK_END);
        }
    }
}
