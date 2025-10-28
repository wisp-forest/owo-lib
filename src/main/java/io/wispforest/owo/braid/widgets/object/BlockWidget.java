package io.wispforest.owo.braid.widgets.object;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.mixin.ui.access.BlockEntityAccessor;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ErrorReporter;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.function.Consumer;

public class BlockWidget extends StatefulWidget {

    public final BlockState blockState;
    public final @Nullable BlockEntity blockEntity;
    public final @Nullable NbtCompound blockEntityNbt;
    public final @Nullable Consumer<Matrix4f> transform;

    private BlockWidget(BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable NbtCompound blockEntityNbt, @Nullable Consumer<Matrix4f> transform) {
        this.blockState = blockState;
        this.blockEntity = blockEntity;
        this.blockEntityNbt = blockEntityNbt;
        this.transform = transform;
    }

    public BlockWidget(BlockState blockState, @Nullable BlockEntity blockEntity) {
        this(blockState, blockEntity, null, null);
    }

    public BlockWidget(BlockState blockState, @Nullable BlockEntity blockEntity, Consumer<Matrix4f> transform) {
        this(blockState, blockEntity, null, transform);
    }

    public BlockWidget(BlockState blockState, @Nullable NbtCompound blockEntityNbt) {
        this(blockState, null, blockEntityNbt, null);
    }

    public BlockWidget(BlockState blockState, @Nullable NbtCompound blockEntityNbt, Consumer<Matrix4f> transform) {
        this(blockState, null, blockEntityNbt, transform);
    }

    public BlockWidget(BlockState blockState, Consumer<Matrix4f> transform) {
        this(blockState, null, null, transform);
    }

    public BlockWidget(BlockState blockState) {
        this(blockState, null, null, null);
    }

    @Override
    public WidgetState<BlockWidget> createState() {
        return new State();
    }

    public static class State extends WidgetState<BlockWidget> {

        private @Nullable BlockEntity internalBlockEntity;

        @Override
        public void init() {
            this.resetBlockEntity();
        }

        @Override
        public void didUpdateWidget(BlockWidget oldWidget) {
            if (this.widget().blockState == oldWidget.blockState
                && this.widget().blockEntity == oldWidget.blockEntity
                && Objects.equals(this.widget().blockEntityNbt, oldWidget.blockEntityNbt)) {
                return;
            }

            this.resetBlockEntity();
        }

        private void resetBlockEntity() {
            this.internalBlockEntity = this.widget().blockEntity == null
                ? prepareBlockEntity(this.widget().blockState, this.widget().blockEntityNbt)
                : null;
        }

        @Override
        public Widget build(BuildContext context) {
            return new RawBlockWidget(
                this.widget().blockState,
                this.internalBlockEntity != null ? this.internalBlockEntity : this.widget().blockEntity,
                this.widget().transform
            );
        }

        // ---

        private static @Nullable BlockEntity prepareBlockEntity(BlockState state, @Nullable NbtCompound nbt) {
            var client = MinecraftClient.getInstance();
            if (!state.hasBlockEntity()) {
                return null;
            }

            var blockEntity = ((BlockEntityProvider) state.getBlock()).createBlockEntity(client.player.getBlockPos(), state);
            if (blockEntity == null) {
                return null;
            }

            ((BlockEntityAccessor) blockEntity).owo$setCachedState(state);
            blockEntity.setWorld(client.world);

            if (nbt != null) {
                blockEntity.read(nbt, client.world.getRegistryManager());
            }

            return blockEntity;
        }
    }
}
