package io.wispforest.uwu.blockentity;

import io.wispforest.owo.blockentity.LinearProcess;
import io.wispforest.owo.blockentity.LinearProcessExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ProcessBlockEntity extends BlockEntity {

    public static final LinearProcess<ProcessBlockEntity> PROCESS = new LinearProcess<>(30);

    private final LinearProcessExecutor<ProcessBlockEntity> executor;

    public ProcessBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.executor = PROCESS.createExecutor(this);
    }

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        PROCESS.configureExecutor(this.executor, world.isClientSide);
    }

    public void tick() {
        this.executor.tick();
    }

    static {

    }
}
