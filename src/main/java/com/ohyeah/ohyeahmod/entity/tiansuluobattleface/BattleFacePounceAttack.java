package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 战颜唯一的主动攻击动作：宣言结束后靠近目标，进入近身窗口再扑击。
 */
public final class BattleFacePounceAttack extends Goal {
    private final TiansuluoBattleFaceEntity entity;
    private LivingEntity committedTarget;
    private int flightTicksRemaining;
    private boolean launched;
    private boolean resolved;

    public BattleFacePounceAttack(TiansuluoBattleFaceEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.entity.getTarget();
        return this.entity.state().getRetaliationTicksRemaining() > 0
                && this.entity.state().getPounceCooldownTicks() <= 0
                && this.entity.onGround()
                && target != null
                && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.resolved
                && this.entity.state().getRetaliationTicksRemaining() > 0
                && this.committedTarget != null
                && this.entity.getTarget() == this.committedTarget
                && this.committedTarget.isAlive();
    }

    @Override
    public void start() {
        this.committedTarget = this.entity.getTarget();
        this.flightTicksRemaining = BattleFaceProfile.POUNCE_MAX_FLIGHT_TICKS;
        this.launched = false;
        this.resolved = false;
    }

    @Override
    public void stop() {
        this.entity.getNavigation().stop();
        if (this.committedTarget != null
                && !this.resolved
                && (this.entity.state().getRetaliationTicksRemaining() > 0 || this.entity.getTarget() != null)) {
            this.finishAttempt();
        }
        this.committedTarget = null;
        this.flightTicksRemaining = 0;
        this.launched = false;
    }

    @Override
    public void tick() {
        LivingEntity target = this.entity.getTarget();
        if (target == null || target != this.committedTarget || !target.isAlive()) {
            this.finishAttempt();
            return;
        }

        this.entity.getLookControl().setLookAt(target, 22.0F, 22.0F);

        if (!this.launched) {
            if (this.entity.state().getRetaliationDeclareTicksRemaining() > 0) {
                this.entity.getNavigation().stop();
                return;
            }
            if (!this.isWithinPounceWindow(target)) {
                this.entity.getNavigation().moveTo(target, 1.2D);
                return;
            }
            this.entity.getNavigation().stop();
            if (this.tryResolveHit(target)) {
                return;
            }
            this.launchAt(target);
            return;
        }

        if (this.tryResolveHit(target)) {
            return;
        }

        this.flightTicksRemaining--;
        if (this.entity.onGround() || this.flightTicksRemaining <= 0) {
            this.finishAttempt();
        }
    }

    private void launchAt(LivingEntity target) {
        Vec3 targetLead = new Vec3(target.getDeltaMovement().x, 0.0D, target.getDeltaMovement().z)
                .scale(BattleFaceProfile.POUNCE_TARGET_LEAD_TICKS);
        Vec3 aimPoint = target.position().add(targetLead).add(0, target.getBbHeight() * 0.5D, 0);
        Vec3 origin = this.entity.getBoundingBox().getCenter();
        Vec3 delta = aimPoint.subtract(origin);
        Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
        Vec3 horizontalDirection = horizontal.lengthSqr() > 1.0E-7D ? horizontal.normalize() : Vec3.ZERO;

        double horizontalSpeed = Mth.clamp(
                horizontal.length() * 0.4D,
                0.6D,
                BattleFaceProfile.POUNCE_HORIZONTAL_SPEED
        );
        double verticalOffset = Mth.clamp(delta.y * 0.15D, -0.08D, 0.16D);
        double verticalSpeed = Mth.clamp(
                BattleFaceProfile.POUNCE_VERTICAL_SPEED + verticalOffset,
                0.25D,
                0.65D
        );

        this.entity.setDeltaMovement(
                horizontalDirection.x * horizontalSpeed,
                verticalSpeed,
                horizontalDirection.z * horizontalSpeed
        );
        this.entity.hasImpulse = true;
        this.launched = true;
    }

    private boolean tryResolveHit(LivingEntity target) {
        double padding = BattleFaceProfile.POUNCE_HITBOX_PADDING;
        AABB targetHitBox = target.getBoundingBox().inflate(padding);
        AABB currentHitBox = this.entity.getBoundingBox().inflate(padding);
        AABB nextHitBox = currentHitBox.move(this.entity.getDeltaMovement());
        if (!currentHitBox.intersects(targetHitBox) && !nextHitBox.intersects(targetHitBox)) {
            return false;
        }

        this.entity.doHurtTarget(target);
        this.finishAttempt();
        return true;
    }

    private void finishAttempt() {
        if (this.resolved) return;
        this.resolved = true;
        // 野生战颜保留有效仇恨并在冷却后继续扑击；驯化战颜只完成这一轮。
        boolean continueRetaliation = !this.entity.isTame()
                && this.entity.getTarget() != null
                && this.entity.getTarget().isAlive();
        this.entity.state().setRetaliationTicksRemaining(
                continueRetaliation ? BattleFaceProfile.RETALIATION_MEMORY_TICKS : 0
        );
        this.entity.state().setPounceCooldownTicks(BattleFaceProfile.POUNCE_COOLDOWN);
        if (!continueRetaliation) {
            this.entity.setTarget(null);
        }
        this.entity.level().broadcastEntityEvent(this.entity, BattleFaceProfile.EVENT_ATTACK_END);
    }


    private boolean isWithinPounceWindow(LivingEntity target) {
        return this.entity.distanceToSqr(target) <= BattleFaceProfile.POUNCE_WINDOW_SQUARED;
    }
}
