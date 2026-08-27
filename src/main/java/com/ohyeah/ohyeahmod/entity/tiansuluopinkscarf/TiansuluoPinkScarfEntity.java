package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf;

import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementTracker;
import com.ohyeah.ohyeahmod.entity.tiansuluo.TiansuluoPlayerNoticeDetector;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 粉围巾实体。
 *
 * <p>实体只保留 Minecraft 生命周期回调和物种入口。食物、繁殖、产卵、剪刀和
 * 远程攻击分别接入原生回调/Goal；不再通过一个 tick 控制器手工驱动整棵行为树。</p>
 */
public class TiansuluoPinkScarfEntity extends TamableAnimal implements RangedAttackMob, IShearable, PlayerRideableJumping {
    public static final EntityDataAccessor<Boolean> HAS_CARRIED_EGG_BLOCK =
            SynchedEntityData.defineId(TiansuluoPinkScarfEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_SILENCED =
            SynchedEntityData.defineId(TiansuluoPinkScarfEntity.class, EntityDataSerializers.BOOLEAN);

    private static final PinkScarfGoalRegistrar GOAL_REGISTRAR = new PinkScarfGoalRegistrar();
    private static final PinkScarfClientFeedback CLIENT_FEEDBACK = new PinkScarfClientFeedback();

    private final PinkScarfState state = new PinkScarfState();

    /* 反击状态机的服务端计时；宣言完成后才交给 RangedAttackGoal 发射。 */
    private int retaliationTicksRemaining;
    private int retaliationDeclareTicksRemaining;
    private boolean retaliationDeclareStarted;
    private int retaliationBurstShotsFired;
    private int retaliationBurstCooldownTicks;
    private boolean wasBabyLastTick;
    private int riderBurstShotsRemaining;
    private int riderBurstNextShotTick;
    private int riderBurstCooldownTicks;
    private @Nullable LivingEntity riderBurstTarget;

    public TiansuluoPinkScarfEntity(EntityType<? extends TiansuluoPinkScarfEntity> entityType, Level level) {
        super(entityType, level);
        this.setTame(false, false);
    }

    /** 禁用原版 FollowOwnerGoal 的 12 格自动瞬移，改为只使用寻路跟随。 */
    @Override
    public boolean shouldTryTeleportToOwner() {
        return false;
    }


    public static boolean canSpawn(EntityType<? extends Animal> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Animal.checkAnimalSpawnRules(type, level, spawnType, pos, random);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PinkScarfProfile.createAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        PinkScarfState.defineSynchedData(builder);
    }

    @Override
    protected void registerGoals() {
        GOAL_REGISTRAR.registerGoals(this);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (spawnType == MobSpawnType.SPAWN_EGG || spawnType == MobSpawnType.BREEDING) {
            this.setAge(PinkScarfProfile.BABY_AGE_TICKS);
        }
        return result;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            CLIENT_FEEDBACK.tickClient(this);
            return;
        }
        ModAdvancementTracker.checkEncounter(this, ModAdvancementIds.MEET_SCARF_LUO, "scarf_luo");
        this.tickPlayerNotice();
        this.state.retaliationAnger().tick(this.level().getGameTime(), this.getMaxHealth());
        this.tickRetaliationWindow();
        this.tickRiderBurst();
        if (this.wasBabyLastTick && !this.isBaby()) {
            this.level().broadcastEntityEvent(this, PinkScarfProfile.EVENT_GROW_UP);
        }
        this.wasBabyLastTick = this.isBaby();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isAlive()) {
            this.state.retaliationAnger().recordHit(
                    amount,
                    this.getMaxHealth(),
                    this.level().getGameTime()
            );
            if (!this.isRetaliating()) {
                this.level().broadcastEntityEvent(this, PinkScarfProfile.EVENT_HURT);
            }
            if (!this.isRiddenByOwner()
                    && !this.isRetaliating()
                    && source.getEntity() instanceof LivingEntity attacker
                    && this.canRetaliateAgainst(attacker)
                    && this.state.retaliationAnger().shouldTrigger(
                            this.getHealth(),
                            this.getMaxHealth(),
                            this.getRandom())) {
                this.beginHurtRetaliation(attacker);
            }
        }
        return hurt;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!this.canFireRetaliationAt(target)) {
            return;
        }

        Vec3 targetAim = new Vec3(
                target.getX() - this.getX(),
                target.getY() + target.getBbHeight() * PinkScarfProfile.PROJECTILE_TARGET_HEIGHT_RATIO - this.getY(),
                target.getZ() - this.getZ()
        );
        Vec3Muzzle muzzle = this.getProjectileMuzzle(targetAim);
        double deltaX = target.getX() - muzzle.x;
        double deltaY = target.getY() + target.getBbHeight() * PinkScarfProfile.PROJECTILE_TARGET_HEIGHT_RATIO - muzzle.y;
        double deltaZ = target.getZ() - muzzle.z;
        double arcBoost = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 0.2D;
        this.fireProjectile(
                muzzle,
                new Vec3(deltaX, deltaY + arcBoost, deltaZ),
                true,
                PinkScarfProfile.PROJECTILE_INACCURACY,
                false
        );
        this.retaliationBurstShotsFired++;
        if (this.retaliationBurstShotsFired >= PinkScarfProfile.BURST_SHOTS) {
            this.retaliationBurstCooldownTicks = PinkScarfProfile.BURST_COOLDOWN_TICKS;
        }
    }


    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return !this.isBaby()
                && this.isTame()
                && passenger instanceof Player player
                && this.isOwnedBy(player)
                && !this.isVehicle();
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof Player player && this.isOwnedBy(player) ? player : null;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        return new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.9D, this.getZ());
    }

    @Override
    protected void tickRidden(Player rider, Vec3 input) {
        super.tickRidden(rider, input);
        this.setRot(rider.getYRot(), rider.getXRot() * 0.5F);
        this.yRotO = this.getYRot();
        this.yHeadRot = this.getYRot();
        this.yBodyRot = this.getYRot();
    }
    @Override
    protected Vec3 getRiddenInput(Player rider, Vec3 input) {
        float strafe = rider.xxa * 0.5F;
        float forward = rider.zza;
        if (forward <= 0.0F) {
            forward *= 0.25F;
        }
        return new Vec3(strafe, 0.0D, forward);
    }

    @Override
    protected float getRiddenSpeed(Player rider) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public void onPlayerJump(int jumpStrength) {
        if (!this.canJump()) {
            return;
        }
        float charge = Mth.clamp(jumpStrength, 0, 90) / 90.0F;
        double jumpVelocity = 0.42D + 0.4D * charge;
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x, jumpVelocity, movement.z);
        this.hasImpulse = true;
    }

    @Override
    public boolean canJump() {
        return this.isRiddenByOwner();
    }

    @Override
    public void handleStartJump(int jumpStrength) {
    }

    @Override
    public void handleStopJump() {
    }


    public boolean isRiddenByOwner() {
        return this.getControllingPassenger() != null;
    }

    public boolean tryStartRiderBurst(Player rider, Entity targetEntity) {
        if (!(targetEntity instanceof LivingEntity target)
                || target == this
                || (target instanceof TamableAnimal tame && tame.isOwnedBy(rider))
                || !target.isAlive()
                || this.level().isClientSide
                || this.isBaby()
                || !this.isTame()
                || !this.isOwnedBy(rider)
                || this.getFirstPassenger() != rider
                || this.riderBurstShotsRemaining > 0
                || this.riderBurstCooldownTicks > 0) {
            return false;
        }

        this.riderBurstTarget = target;
        this.riderBurstShotsRemaining = PinkScarfProfile.RIDER_BURST_SHOTS;
        this.riderBurstNextShotTick = this.tickCount;
        this.fireMountedBurstShot();
        if (rider instanceof ServerPlayer serverPlayer) {
            ModAdvancementTracker.award(serverPlayer, ModAdvancementIds.RIDER_ATTACK);
        }
        return true;
    }

    private void tickRiderBurst() {
        if (this.riderBurstCooldownTicks > 0) {
            this.riderBurstCooldownTicks--;
        }
        if (this.riderBurstShotsRemaining <= 0) {
            return;
        }
        if (!this.isRiddenByOwner()
                || this.riderBurstTarget == null
                || !this.riderBurstTarget.isAlive()) {
            this.riderBurstShotsRemaining = 0;
            this.riderBurstNextShotTick = 0;
            this.riderBurstTarget = null;
            this.riderBurstCooldownTicks = PinkScarfProfile.BURST_COOLDOWN_TICKS;
            return;
        }
        if (this.tickCount < this.riderBurstNextShotTick) {
            return;
        }
        this.fireMountedBurstShot();
    }

    private void fireMountedBurstShot() {
        if (this.riderBurstShotsRemaining <= 0 || this.riderBurstTarget == null) {
            return;
        }
        LivingEntity target = this.riderBurstTarget;
        Vec3 targetAim = new Vec3(
                target.getX() - this.getX(),
                target.getY() + target.getBbHeight() * PinkScarfProfile.PROJECTILE_TARGET_HEIGHT_RATIO - this.getY(),
                target.getZ() - this.getZ()
        );
        Vec3Muzzle muzzle = this.getProjectileMuzzle(targetAim);
        double deltaX = target.getX() - muzzle.x;
        double deltaY = target.getY() + target.getBbHeight() * PinkScarfProfile.PROJECTILE_TARGET_HEIGHT_RATIO - muzzle.y;
        double deltaZ = target.getZ() - muzzle.z;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double arcBoost = horizontalDistance * PinkScarfProfile.RIDER_ARC_BOOST_PER_BLOCK;
        this.fireProjectile(
                muzzle,
                new Vec3(deltaX, deltaY + arcBoost, deltaZ),
                true,
                0.0F,
                false
        );
        this.riderBurstShotsRemaining--;
        if (this.riderBurstShotsRemaining > 0) {
            this.riderBurstNextShotTick = this.tickCount + PinkScarfProfile.BURST_INTERVAL_TICKS;
        } else {
            this.riderBurstTarget = null;
            this.riderBurstCooldownTicks = PinkScarfProfile.BURST_COOLDOWN_TICKS;
        }
    }

    private void fireProjectile(
            Vec3Muzzle muzzle,
            Vec3 direction,
            boolean playSound,
            float inaccuracy,
            boolean noGravity
    ) {
        if (this.level().isClientSide || direction.lengthSqr() < 1.0E-6D) {
            return;
        }
        PinkScarfProjectileEntity projectile = new PinkScarfProjectileEntity(this.level(), this);
        projectile.setDamage((float) PinkScarfProfile.PROJECTILE_DAMAGE);
        projectile.setPos(muzzle.x, muzzle.y, muzzle.z);
        projectile.setNoGravity(noGravity);
        projectile.shoot(
                direction.x,
                direction.y,
                direction.z,
                PinkScarfProfile.PROJECTILE_SPEED,
                inaccuracy
        );
        this.level().addFreshEntity(projectile);
        if (playSound) {
            this.level().broadcastEntityEvent(this, PinkScarfProfile.EVENT_ATTACK_SHOT);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide && this.isDeadOrDying()) {
            this.level().broadcastEntityEvent(this, PinkScarfProfile.EVENT_DEATH);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (!this.isBaby()) {
            this.spawnAtLocation(ModItems.CHIPS.get());
            this.spawnAtLocation(new ItemStack(
                    ModItems.TIANSULUO_PINK_SCARF_EGG.get(),
                    PinkScarfProfile.randomLuanluanCount(this.getRandom())
            ));
        }
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status >= PinkScarfProfile.EVENT_ATTACK_DECLARE
                && status <= PinkScarfProfile.EVENT_LUANLUAN_BLOCK_BROKEN) {
            if (this.level().isClientSide) {
                CLIENT_FEEDBACK.handleClientEntityEvent(this, status);
            }
            return;
        }
        super.handleEntityEvent(status);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean favorite = PinkScarfProfile.FOODS.isFavorite(stack);

        if (this.isTame() && this.isOwnedBy(player) && !this.isBaby() && stack.isEmpty()) {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(false);
                this.finishRetaliation();
                if (player.startRiding(this) && player instanceof ServerPlayer serverPlayer) {
                    ModAdvancementTracker.award(serverPlayer, ModAdvancementIds.RIDE_SCARF_LUO);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        /* 驯服沿用 Cat/Wolf 的交互形态：最爱食物只在未驯服成年体上负责驯服。 */
        if (!this.isTame() && !this.isBaby() && favorite) {
            if (this.level().isClientSide) {
                return InteractionResult.CONSUME;
            }

            boolean wasSilenced = this.state.isSilenced(this);
            this.usePlayerItem(player, hand, stack);
            this.tryToTame(player);
            this.state.setSilenced(this, false);
            if (wasSilenced) {
                this.awardAdvancement(player, ModAdvancementIds.RESTORE_VOICE);
            }
            this.playFoodFeedback(true);
            return InteractionResult.SUCCESS;
        }

        /* 幼体成长沿用 AgeableMob 的 age 单位：喜欢食物推进 6000 tick，最爱食物直接成年。 */
        if (this.isBaby() && this.isFood(stack)) {
            if (this.level().isClientSide) {
                return InteractionResult.CONSUME;
            }

            boolean wasBaby = this.isBaby();
            boolean wasSilenced = this.state.isSilenced(this);
            this.usePlayerItem(player, hand, stack);
            if (favorite) {
                this.setAge(0);
            } else {
                this.ageUp(PinkScarfProfile.LIKED_FOOD_GROWTH_TICKS / 20, true);
            }
            this.state.setSilenced(this, false);
            this.awardAdvancement(player, ModAdvancementIds.FEED_GROW);
            if (wasSilenced) {
                this.awardAdvancement(player, ModAdvancementIds.RESTORE_VOICE);
            }
            if (wasBaby && !this.isBaby()) {
                this.level().broadcastEntityEvent(this, PinkScarfProfile.EVENT_GROW_UP);
                this.wasBabyLastTick = false;
            }
            this.playFoodFeedback(favorite);
            return InteractionResult.SUCCESS;
        }

        /* 驯服后的主人使用食物时，先走原生宠物治疗习惯，再交给 Animal 处理求偶。 */
        if (this.isTame() && this.isOwnedBy(player) && !this.isBaby() && this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
            if (this.level().isClientSide) {
                return InteractionResult.CONSUME;
            }

            boolean wasSilenced = this.state.isSilenced(this);
            this.usePlayerItem(player, hand, stack);
            this.heal(PinkScarfProfile.FOODS.isFavorite(stack)
                    ? PinkScarfProfile.FOOD_HEAL_AMOUNT
                    : PinkScarfProfile.FOOD_HEAL_AMOUNT * 0.5F);
            this.state.setSilenced(this, false);
            if (wasSilenced) {
                this.awardAdvancement(player, ModAdvancementIds.RESTORE_VOICE);
            }
            this.playFoodFeedback(favorite);
            return InteractionResult.SUCCESS;
        }
        if (!this.isTame() && !this.isBaby() && this.isFood(stack)) {
            return InteractionResult.PASS;
        }

        /* Animal.mobInteract 负责成年求偶，避免手写 love 计时器。 */
        boolean wasSilenced = this.state.isSilenced(this);
        InteractionResult result = super.mobInteract(player, hand);
        if (result.consumesAction() && this.isFood(stack) && !this.level().isClientSide) {
            this.state.setSilenced(this, false);
            if (wasSilenced) {
                this.awardAdvancement(player, ModAdvancementIds.RESTORE_VOICE);
            }
            this.playFoodFeedback(favorite);
        }
        return result;
    }

    private void tryToTame(Player player) {
        if (this.getRandom().nextInt(3) == 0 && !EventHooks.onAnimalTame(this, player)) {
            this.tame(player);
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTracker.award(serverPlayer, ModAdvancementIds.TAME_SCARF_LUO);
            }
            this.getNavigation().stop();
            this.setTarget(null);
            this.level().broadcastEntityEvent(this, (byte) 7);
        }
    }

    private void tickPlayerNotice() {
        if (this.state.hasNoticedPlayer()
                || this.state.isSilenced(this)
                || this.tickCount % 5 != 0) {
            return;
        }
        if (TiansuluoPlayerNoticeDetector.seesPlayer(this)) {
            this.state.setNoticedPlayer(true);
            this.level().broadcastEntityEvent(this, PinkScarfProfile.EVENT_NOTICE_PLAYER);
        }
    }

    private void playFoodFeedback(boolean favorite) {
        this.level().broadcastEntityEvent(this, favorite ? PinkScarfProfile.EVENT_EAT_FAVORITE : PinkScarfProfile.EVENT_EAT);
    }

    private void awardAdvancement(Player player, String advancementId) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancementTracker.award(serverPlayer, advancementId);
        }
    }

    @Override
    public boolean canFallInLove() {
        return this.isTame()
                && !this.isOrderedToSit()
                && !this.state.hasCarriedEggBlock(this)
                && super.canFallInLove();
    }

    @Override
    public boolean canMate(Animal other) {
        return other != this
                && other.getClass() == this.getClass()
                && other instanceof TiansuluoPinkScarfEntity partner
                && this.isTame()
                && partner.isTame()
                && !this.isOrderedToSit()
                && !partner.isOrderedToSit()
                && !this.state.hasCarriedEggBlock(this)
                && !partner.state.hasCarriedEggBlock(partner)
                && this.isInLove()
                && partner.isInLove();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntityTypes.TIANSULUO_PINK_SCARF.get().create(level);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return PinkScarfProfile.FOODS.isFood(stack);
    }

    public boolean isFavoriteFood(ItemStack stack) {
        return PinkScarfProfile.FOODS.isFavorite(stack);
    }

    /* -------------------- IShearable：交给 NeoForge 的 ShearsItem 统一处理 -------------------- */

    @Override
    public boolean isShearable(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        return this.isAlive() && !this.state.isSilenced(this);
    }

    @Override
    public List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        if (!level.isClientSide) {
            level.playSound(null, this, net.minecraft.sounds.SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            this.state.setSilenced(this, true);
            level.broadcastEntityEvent(this, PinkScarfProfile.EVENT_SHEAR_REACT);
            this.awardAdvancement(player, ModAdvancementIds.SHEAR);
        }
        return List.of(new ItemStack(Items.RED_WOOL));
    }

    /* -------------------- 供 PinkScarfRetaliationGoal 使用的最小状态接口 -------------------- */

    boolean canRetaliateAgainst(@Nullable LivingEntity attacker) {
        if (attacker == null || attacker == this || !attacker.isAlive()) {
            return false;
        }
        return true;
    }

    void beginOwnerRetaliation(LivingEntity target) {
        this.beginRetaliation(target);
    }

    void beginHurtRetaliation(LivingEntity attacker) {
        this.beginRetaliation(attacker);
    }

    private void beginRetaliation(LivingEntity target) {
        this.setTarget(target);
        this.state.retaliationAnger().reset();
        this.retaliationTicksRemaining = PinkScarfProfile.RETALIATION_MEMORY_TICKS;
        this.retaliationDeclareTicksRemaining = 0;
        this.retaliationDeclareStarted = false;
        this.retaliationBurstShotsFired = 0;
        this.retaliationBurstCooldownTicks = 0;
        this.beginRetaliationDeclaration();
    }

    private void beginRetaliationDeclaration() {
        if (this.retaliationDeclareStarted) {
            return;
        }

        this.retaliationDeclareStarted = true;
        if (this.isSilent() || this.state.isSilenced(this)) {
            this.retaliationDeclareTicksRemaining = 0;
            return;
        }

        this.retaliationDeclareTicksRemaining = PinkScarfProfile.ATTACK_DECLARE_TICKS;
        this.level().broadcastEntityEvent(this, PinkScarfProfile.EVENT_ATTACK_DECLARE);
    }

    void tickRetaliationWindow() {
        if (this.retaliationTicksRemaining <= 0) {
            this.finishRetaliation();
            return;
        }

        LivingEntity target = this.getTarget();
        if (!this.canRetaliateAgainst(target)
                || this.distanceToSqr(target) > (double) PinkScarfProfile.RETALIATION_RANGE * PinkScarfProfile.RETALIATION_RANGE) {
            this.finishRetaliation();
            return;
        }

        this.getLookControl().setLookAt(target, 30.0F, 30.0F);


        if (!this.retaliationDeclareStarted) {
            this.beginRetaliationDeclaration();
            return;
        }

        if (this.retaliationDeclareTicksRemaining > 0) {
            this.retaliationDeclareTicksRemaining--;
            return;
        }

        if (this.retaliationBurstCooldownTicks > 0) {
            this.retaliationBurstCooldownTicks--;
        }
        if (this.retaliationBurstShotsFired >= PinkScarfProfile.BURST_SHOTS
                && this.retaliationBurstCooldownTicks <= 0) {
            this.finishRetaliation();
            return;
        }

        this.retaliationTicksRemaining--;
        if (this.retaliationTicksRemaining <= 0) {
            this.finishRetaliation();
        }
    }

    boolean isRetaliating() {
        return this.retaliationTicksRemaining > 0 && this.getTarget() != null;
    }


    boolean isRetaliationDeclaring() {
        return this.retaliationDeclareStarted && this.retaliationDeclareTicksRemaining > 0;
    }

    boolean canFireRetaliationAt(LivingEntity target) {
        return this.isRetaliating()
                && !this.isRetaliationDeclaring()
                && this.retaliationBurstCooldownTicks <= 0
                && target == this.getTarget();
    }

    void finishRetaliation() {
        boolean wasActive = this.retaliationTicksRemaining > 0
                || this.retaliationDeclareStarted
                || this.getTarget() != null;
        boolean shouldPlayEnd = this.retaliationDeclareStarted
                && (this.retaliationDeclareTicksRemaining > 0 || this.retaliationBurstShotsFired > 0);
        this.retaliationTicksRemaining = 0;
        if (wasActive) {
            this.state.retaliationAnger().reset();
        }
        this.retaliationDeclareTicksRemaining = 0;
        this.retaliationDeclareStarted = false;
        this.retaliationBurstShotsFired = 0;
        this.retaliationBurstCooldownTicks = 0;
        this.setTarget(null);
        if (shouldPlayEnd && !this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, PinkScarfProfile.EVENT_ATTACK_END);
        }
    }

    private Vec3Muzzle getProjectileMuzzle(Vec3 direction) {
        Vec3 forward = new Vec3(direction.x, 0.0D, direction.z);
        if (forward.lengthSqr() < 1.0E-6D) {
            forward = Vec3.directionFromRotation(0.0F, this.getYHeadRot());
        } else {
            forward = forward.normalize();
        }
        double horizontalOffset = this.getBbWidth() * 0.5D + PinkScarfProfile.PROJECTILE_FRONT_OFFSET;
        return new Vec3Muzzle(
                this.getX() + forward.x * horizontalOffset,
                this.getY() + this.getBbHeight() * PinkScarfProfile.PROJECTILE_MUZZLE_HEIGHT_RATIO,
                this.getZ() + forward.z * horizontalOffset
        );
    }

    public PinkScarfState state() {
        return this.state;
    }

    private record Vec3Muzzle(double x, double y, double z) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        this.state.addAdditionalSaveData(this, nbt);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.state.readAdditionalSaveData(this, nbt);
    }
}
