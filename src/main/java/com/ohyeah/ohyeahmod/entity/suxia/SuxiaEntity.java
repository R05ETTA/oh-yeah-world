package com.ohyeah.ohyeahmod.entity.suxia;

import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 素虾：一个保持轻量的原生水生生物。
 *
 * <p>它不加入天素罗的驯服、繁殖和状态系统；核心闭环只有水中生成、游动、
 * 栾栾发射、专属掉落和基础声音。</p>
 */
public class SuxiaEntity extends WaterAnimal {
    private float tentacleAngle;
    private float prevTentacleAngle;
    private float swimSpeed;
    private float prevSwimProgress;
    private float swimProgress;
    private final SuxiaClientFeedback feedback = new SuxiaClientFeedback();
    public SuxiaEntity(EntityType<? extends SuxiaEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.moveControl = new SuxiaMoveControl(this);
    }

    public static boolean canSpawn(
            EntityType<? extends WaterAnimal> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        return WaterAnimal.checkSurfaceWaterAnimalSpawnRules(type, level, spawnType, pos, random);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SuxiaProfile.createAttributes();
    }

    @Override
    protected void registerGoals() {
        new SuxiaGoalRegistrar().registerGoals(this);
    }

    public boolean isFood(ItemStack stack) {
        return SuxiaProfile.FOODS.isFood(stack);
    }

    public boolean isFavoriteFood(ItemStack stack) {
        return SuxiaProfile.FOODS.isFavorite(stack);
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void die(DamageSource source) {
        boolean shouldFireLuanluan = !this.level().isClientSide && this.isInWaterOrBubble();
        super.die(source);
        if (shouldFireLuanluan) {
            this.level().broadcastEntityEvent(this, SuxiaProfile.EVENT_DEATH);
            this.fireLuanluanAt(source.getEntity());
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isAlive() && this.isInWaterOrBubble()) {
            this.level().broadcastEntityEvent(this, SuxiaProfile.EVENT_HURT);
            this.fireLuanluanAt(source.getEntity());
        }
        return hurt;
    }


    private void fireLuanluanAt(Entity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || !(target instanceof LivingEntity livingTarget)
                || !livingTarget.isAlive()) {
            return;
        }

        Vec3 muzzle = new Vec3(
                this.getX(),
                this.getY() + this.getBbHeight() * 0.8D,
                this.getZ()
        );
        double deltaX = livingTarget.getX() - muzzle.x;
        double deltaY = livingTarget.getY() + livingTarget.getBbHeight() * 0.5D - muzzle.y;
        double deltaZ = livingTarget.getZ() - muzzle.z;

        SuxiaLuanluanProjectileEntity projectile = new SuxiaLuanluanProjectileEntity(serverLevel, this);
        projectile.setDamage(SuxiaProfile.LUANLUAN_PROJECTILE_DAMAGE);
        projectile.setPos(muzzle.x, muzzle.y, muzzle.z);
        projectile.shoot(
                deltaX,
                deltaY,
                deltaZ,
                SuxiaProfile.LUANLUAN_PROJECTILE_SPEED,
                0.0F
        );
        serverLevel.addFreshEntity(projectile);
        serverLevel.broadcastEntityEvent(this, SuxiaProfile.EVENT_LUANLUAN_SHOT);
        ModAdvancementTracker.awardNearby(serverLevel, this.blockPosition(), ModAdvancementIds.SUXIA_LUANLUAN_SHOT);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            this.feedback.tickClient(this);
        }
        ModAdvancementTracker.checkEncounter(this, ModAdvancementIds.MEET_SUXIA, "suxia");

        this.prevTentacleAngle = this.tentacleAngle;
        this.prevSwimProgress = this.swimProgress;

        boolean inWater = this.isInWaterOrBubble();
        if (inWater) {
            this.swimProgress = Math.min(1.0F, this.swimProgress + 0.05F);
            this.swimSpeed += (0.4F - this.swimSpeed) * 0.1F;
        } else {
            this.swimProgress = Math.max(0.0F, this.swimProgress - 0.08F);
            this.swimSpeed = Math.max(0.0F, this.swimSpeed - 0.09F);
            if (this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(
                        (this.random.nextFloat() * 2.0F - 1.0F) * 0.05F,
                        0.0D,
                        (this.random.nextFloat() * 2.0F - 1.0F) * 0.05F
                ));
            }
        }

        this.tentacleAngle += this.swimSpeed * 0.5F;
        if (this.level().isClientSide
                && inWater
                && this.getDeltaMovement().lengthSqr() > 0.01D
                && this.random.nextInt(10) == 0) {
            float dx = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.4F;
            float dy = this.random.nextFloat() * 0.2F;
            float dz = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.4F;
            this.level().addParticle(
                    ParticleTypes.SPLASH,
                    this.getX() + dx,
                    this.getY() + dy,
                    this.getZ() + dz,
                    0.0D,
                    0.04D,
                    0.0D
            );
        }
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status >= SuxiaProfile.EVENT_HURT && status <= SuxiaProfile.EVENT_LUANLUAN_SHOT) {
            if (this.level().isClientSide) {
                this.feedback.handleClientEntityEvent(this, status);
            }
            return;
        }
        super.handleEntityEvent(status);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.02F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
        } else {
            super.travel(travelVector);
        }
    }

    public float getSwimProgress(float partialTicks) {
        return this.prevSwimProgress + (this.swimProgress - this.prevSwimProgress) * partialTicks;
    }

    public float getTentacleAngle(float partialTicks) {
        return this.prevTentacleAngle + (this.tentacleAngle - this.prevTentacleAngle) * partialTicks;
    }

    static class SuxiaMoveControl extends MoveControl {
        private final SuxiaEntity suxia;

        SuxiaMoveControl(SuxiaEntity suxia) {
            super(suxia);
            this.suxia = suxia;
        }

        @Override
        public void tick() {
            if (this.suxia.isInWater()) {
                this.suxia.setDeltaMovement(this.suxia.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
                if (this.operation == MoveControl.Operation.MOVE_TO && !this.suxia.getNavigation().isDone()) {
                    double dx = this.wantedX - this.suxia.getX();
                    double dy = this.wantedY - this.suxia.getY();
                    double dz = this.wantedZ - this.suxia.getZ();
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance < 0.1D) {
                        this.mob.setSpeed(0.0F);
                        return;
                    }

                    dy /= distance;
                    float targetYRot = (float) (Mth.atan2(dz, dx) * (180.0F / Math.PI)) - 90.0F;
                    this.suxia.setYRot(this.rotlerp(this.suxia.getYRot(), targetYRot, 30.0F));
                    this.suxia.yBodyRot = this.suxia.getYRot();
                    this.suxia.yHeadRot = this.suxia.getYRot();
                    this.suxia.setSpeed((float) (this.speedModifier * 0.15D));
                    this.suxia.setDeltaMovement(this.suxia.getDeltaMovement().add(
                            dx / distance * 0.1D,
                            dy / distance * 0.1D,
                            dz / distance * 0.1D
                    ));
                } else {
                    this.suxia.setSpeed(0.0F);
                }
            } else {
                super.tick();
            }
        }
    }
}
