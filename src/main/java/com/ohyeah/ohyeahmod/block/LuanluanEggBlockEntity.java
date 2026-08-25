package com.ohyeah.ohyeahmod.block;

import com.ohyeah.ohyeahmod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** 保存栾栾块产出者，用于玩家破坏栾栾块时通知对应的天素罗。 */
public final class LuanluanEggBlockEntity extends BlockEntity {
    private static final String TAG_PARENT_UUID = "ParentUUID";

    @Nullable
    private UUID parentUuid;

    public LuanluanEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUANLUAN_EGG.get(), pos, state);
    }

    public void setParentUuid(@Nullable UUID parentUuid) {
        this.parentUuid = parentUuid;
        this.setChanged();
    }

    @Nullable
    public UUID getParentUuid() {
        return this.parentUuid;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.parentUuid = tag.hasUUID(TAG_PARENT_UUID) ? tag.getUUID(TAG_PARENT_UUID) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.parentUuid != null) {
            tag.putUUID(TAG_PARENT_UUID, this.parentUuid);
        }
    }
}
