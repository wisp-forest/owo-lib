package io.wispforest.uwu.block;

import com.mojang.serialization.MapCodec;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.events.KeyPressEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class BraidDisplayBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(
        0, 0, 0, 16, 2, 16
    );

    public BraidDisplayBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        var entity = level.getBlockEntity(pos);
        if (player.isShiftKeyDown() && entity instanceof BraidDisplayBlockEntity display && display.display != null) {
            display.display.app.eventBinding.add(new KeyPressEvent(GLFW.GLFW_KEY_I, GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_I), new KeyModifiers(GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL)));

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BraidDisplayBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }
}
