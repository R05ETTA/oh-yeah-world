package com.ohyeah.ohyeahmod.block;

import com.ohyeah.ohyeahmod.entity.tiansuluobattleface.BattleFaceProfile;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.PinkScarfProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * 栾栾块。
 */
public final class LuanluanEggBlock extends Block {
    public static final IntegerProperty HATCH = IntegerProperty.create("hatch", 0, 2);
    public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 1, 4);

    private static final VoxelShape ONE_EGG_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 12.0D, 7.0D, 12.0D);
    private static final VoxelShape MULTI_EGG_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 7.0D, 15.0D);

    private final String speciesId;
    private final Supplier<? extends EntityType<?>> entityTypeSupplier;

    public LuanluanEggBlock(String speciesId, Supplier<? extends EntityType<?>> entityTypeSupplier, Properties properties) {
        super(properties);
        this.speciesId = speciesId;
        this.entityTypeSupplier = entityTypeSupplier;
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(EGGS, 1));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!this.shouldHatch(level)) return;

        int hatch = state.getValue(HATCH);
        if (hatch < 2) {
            level.setBlock(pos, state.setValue(HATCH, hatch + 1), 2);
        } else {
            this.hatch(level, pos, state);
        }
    }

    private void hatch(ServerLevel level, BlockPos pos, BlockState state) {
        int eggs = state.getValue(EGGS);
        level.removeBlock(pos, false);
        for (int i = 0; i < eggs; i++) {
            var entity = this.entityTypeSupplier.get().create(level);
            if (entity instanceof AgeableMob ageable) {
                ageable.setAge(AgeableMob.BABY_START_AGE);
                ageable.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(ageable);
            }
        }
    }

    private boolean shouldHatch(Level level) {
        int chanceInv = 500;
        if (PinkScarfProfile.SPECIES_ID.equals(this.speciesId)) {
            chanceInv = PinkScarfProfile.HATCH_CHANCE_INV;
        } else if (BattleFaceProfile.SPECIES_ID.equals(this.speciesId)) {
            chanceInv = BattleFaceProfile.HATCH_CHANCE_INV;
        }

        /* 与原版 TurtleEggBlock 一致：夜间窗口稳定推进，其他时间保留随机推进。 */
        float timeOfDay = level.getTimeOfDay(1.0F);
        return timeOfDay > 0.65F && timeOfDay < 0.69F || level.random.nextInt(chanceInv) == 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, EGGS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(EGGS) > 1 ? MULTI_EGG_SHAPE : ONE_EGG_SHAPE;
    }
}
