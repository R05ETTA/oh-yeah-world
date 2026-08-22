package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.spawn;

import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.PinkScarfProfile;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.TiansuluoPinkScarfEntity;
import com.ohyeah.ohyeahmod.entity.logic.SleepWakeSpeciesHandler;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 【粉围巾】睡觉唤醒生成逻辑处理器。
 */
public final class TiansuluoPinkScarfBedWakeSpawner implements SleepWakeSpeciesHandler {
    public static final TiansuluoPinkScarfBedWakeSpawner INSTANCE = new TiansuluoPinkScarfBedWakeSpawner();

    private TiansuluoPinkScarfBedWakeSpawner() {
    }

    @Override
    public String speciesId() {
        return PinkScarfProfile.SPECIES_ID;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean shouldQueueSpawn(ServerPlayer player) {
        if (player == null) return false;
        if (player.getSleepingPos().isEmpty()) return false;
        return isBed(player.serverLevel(), player.getSleepingPos().get());
    }

    @Override
    public boolean canSpawnAt(ServerPlayer player, BlockPos bedPos) {
        return isBed(player.serverLevel(), bedPos);
    }

    @Override
    public void trySpawn(ServerPlayer player, BlockPos bedPos) {
        ServerLevel world = player.serverLevel();
        
        List<BlockPos> spawnPositions = findSpawnPositions(world, bedPos, 4, 2);
        if (spawnPositions.size() < 2) return;

        TiansuluoPinkScarfEntity adult = createEntity(world, spawnPositions.get(0), false);
        TiansuluoPinkScarfEntity baby = createEntity(world, spawnPositions.get(1), true);

        if (adult != null) world.addFreshEntity(adult);
        if (baby != null) world.addFreshEntity(baby);
    }

    private static @Nullable TiansuluoPinkScarfEntity createEntity(ServerLevel world, BlockPos pos, boolean baby) {
        TiansuluoPinkScarfEntity entity = ModEntityTypes.TIANSULUO_PINK_SCARF.get().create(world);
        if (entity == null) return null;

        Vec3 spawnPos = Vec3.atBottomCenterOf(pos);
        entity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, world.getRandom().nextFloat() * 360.0F, 0.0F);
        entity.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
        
        if (baby) {
            entity.setBaby(true);
            entity.setAge(PinkScarfProfile.BABY_AGE_TICKS);
        } else {
            entity.setAge(0);
        }
        return entity;
    }

    private static List<BlockPos> findSpawnPositions(ServerLevel world, BlockPos bedPos, int radius, int needed) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int distance = 1; distance <= radius; distance++) {
            for (int dx = -distance; dx <= distance; dx++) {
                for (int dz = -distance; dz <= distance; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != distance) continue;
                    for (int dy = 0; dy >= -2; dy--) {
                        mutable.set(bedPos.getX() + dx, bedPos.getY() + dy, bedPos.getZ() + dz);
                        if (!isSafeSpawnPos(world, mutable, bedPos)) continue;
                        BlockPos candidate = mutable.immutable();
                        if (!positions.contains(candidate)) {
                            positions.add(candidate);
                            if (positions.size() >= needed) return positions;
                        }
                    }
                }
            }
        }
        return positions;
    }

    private static boolean isSafeSpawnPos(ServerLevel world, BlockPos pos, BlockPos bedPos) {
        return world.isEmptyBlock(pos) && world.isEmptyBlock(pos.above()) && world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP) && pos.distSqr(bedPos) > 1.0D;
    }

    private static boolean isBed(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BedBlock;
    }
}
