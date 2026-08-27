package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * 负责战颜怒气、反击窗口和唯一攻击目标的服务端状态推进。
 *
 * <p>受击只负责累积怒气并播放反馈；怒气判定成功后立即进入“攻击宣言 → 实际攻击”，
 * 不再等待受击语音结束。服务器不依赖任何客户端播放完成回调。</p>
 */
public final class BattleFaceRetaliationController {
    public void onHurt(TiansuluoBattleFaceEntity entity, boolean hurt, DamageSource source, float amount) {
        if (!hurt || entity.level().isClientSide || !entity.isAlive()) {
            return;
        }

        BattleFaceState state = entity.state();
        long gameTime = entity.level().getGameTime();
        state.retaliationAnger().recordHit(amount, entity.getMaxHealth(), gameTime);

        // 正在宣言或攻击时不重启当前攻击链；本次伤害仍然已经计入怒气。
        if (state.getRetaliationTicksRemaining() > 0 && entity.getTarget() != null) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker)
                || attacker == entity
                || !attacker.isAlive()) {
            return;
        }

        if (state.retaliationAnger().shouldTrigger(
                entity.getHealth(),
                entity.getMaxHealth(),
                entity.getRandom())) {
            this.begin(entity, attacker);
        }
    }

    public void onOwnerHurtTarget(TiansuluoBattleFaceEntity entity, LivingEntity target) {
        if (entity.level().isClientSide
                || !entity.isAlive()
                || target == null
                || target == entity
                || !target.isAlive()) {
            return;
        }
        this.begin(entity, target);
    }

    private void begin(TiansuluoBattleFaceEntity entity, LivingEntity target) {
        BattleFaceState state = entity.state();
        state.setRetaliationTicksRemaining(BattleFaceProfile.RETALIATION_TOTAL_TICKS);
        state.setRetaliationDeclareTicksRemaining(0);
        state.setRetaliationDeclareStarted(false);
        state.setPounceCooldownTicks(0);
        state.retaliationAnger().reset();
        entity.setTarget(target);
        this.beginDeclaration(entity);
    }

    private void beginDeclaration(TiansuluoBattleFaceEntity entity) {
        BattleFaceState state = entity.state();
        if (state.isRetaliationDeclareStarted()) {
            return;
        }

        state.setRetaliationDeclareStarted(true);
        if (entity.isSilent() || state.isSilenced(entity)) {
            state.setRetaliationDeclareTicksRemaining(0);
            return;
        }

        state.setRetaliationDeclareTicksRemaining(BattleFaceProfile.RETALIATION_DECLARE_TICKS);
        entity.level().broadcastEntityEvent(entity, BattleFaceProfile.EVENT_ATTACK_DECLARE);
    }

    public void tick(TiansuluoBattleFaceEntity entity) {
        if (entity.level().isClientSide) {
            return;
        }

        BattleFaceState state = entity.state();
        if (state.getRetaliationTicksRemaining() <= 0) {
            this.finish(entity);
            return;
        }

        LivingEntity target = entity.getTarget();
        if (target == null
                || !target.isAlive()
                || entity.distanceToSqr(target) > BattleFaceProfile.RETALIATION_MAX_RANGE_SQUARED) {
            this.finish(entity);
            return;
        }

        entity.getLookControl().setLookAt(target, 22.0F, 22.0F);
        if (!state.isRetaliationDeclareStarted()) {
            this.beginDeclaration(entity);
            return;
        }

        if (state.getRetaliationDeclareTicksRemaining() > 0) {
            state.decrementRetaliationDeclareTicks();
            return;
        }

        state.decrementRetaliationTicks();
        if (state.getRetaliationTicksRemaining() <= 0) {
            this.finish(entity);
        }
    }

    private void finish(TiansuluoBattleFaceEntity entity) {
        BattleFaceState state = entity.state();
        boolean wasActive = state.getRetaliationTicksRemaining() > 0
                || state.isRetaliationDeclareStarted()
                || entity.getTarget() != null;
        boolean shouldPlayEnd = state.isRetaliationDeclareStarted()
                && state.getRetaliationDeclareTicksRemaining() > 0;
        state.setRetaliationTicksRemaining(0);
        state.setRetaliationDeclareTicksRemaining(0);
        state.setRetaliationDeclareStarted(false);
        if (wasActive) {
            state.retaliationAnger().reset();
        }
        entity.setTarget(null);
        if (shouldPlayEnd) {
            entity.level().broadcastEntityEvent(entity, BattleFaceProfile.EVENT_ATTACK_END);
        }
    }
}
