package io.wispforest.uwu.blockentity;

import io.wispforest.owo.blockentity.LinearProcess;
import io.wispforest.owo.blockentity.LinearProcessExecutor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ProcessBlockEntity extends BlockEntity {

    public static final LinearProcess<ProcessBlockEntity> PROCESS = new LinearProcess<>(30);

    private final LinearProcessExecutor<ProcessBlockEntity> executor;

    public ProcessBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.executor = PROCESS.createExecutor(this);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        PROCESS.configureExecutor(this.executor, world.isClient);
    }

    public void tick() {
        this.executor.tick();
    }

    static {

    }
}
