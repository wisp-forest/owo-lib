package io.wispforest.owo.braid.widgets.object;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.mixin.ui.access.BlockEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.function.Consumer;

public class BlockWidget extends StatefulWidget {

    public final BlockState blockState;
    public final @Nullable BlockEntity blockEntity;
    public final @Nullable CompoundTag blockEntityNbt;
    public final @Nullable Consumer<Matrix4f> transform;

    private BlockWidget(BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable CompoundTag blockEntityNbt, @Nullable Consumer<Matrix4f> transform) {
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

    public BlockWidget(BlockState blockState, @Nullable CompoundTag blockEntityNbt) {
        this(blockState, null, blockEntityNbt, null);
    }

    public BlockWidget(BlockState blockState, @Nullable CompoundTag blockEntityNbt, Consumer<Matrix4f> transform) {
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

        private static @Nullable BlockEntity prepareBlockEntity(BlockState state, @Nullable CompoundTag nbt) {
            var client = Minecraft.getInstance();
            if (!state.hasBlockEntity()) {
                return null;
            }

            var blockEntity = ((EntityBlock) state.getBlock()).newBlockEntity(client.player.blockPosition(), state);
            if (blockEntity == null) {
                return null;
            }

            ((BlockEntityAccessor) blockEntity).owo$setBlockState(state);
            blockEntity.setLevel(client.level);

            if (nbt != null) {
                blockEntity.loadWithComponents(TagValueInput.create(new ProblemReporter.ScopedCollector(Owo.LOGGER), client.level.registryAccess(), nbt));
            }

            return blockEntity;
        }
    }
}
