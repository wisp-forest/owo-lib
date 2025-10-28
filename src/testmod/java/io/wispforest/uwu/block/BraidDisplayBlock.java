package io.wispforest.uwu.block;

import com.mojang.serialization.MapCodec;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.events.KeyPressEvent;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class BraidDisplayBlock extends BlockWithEntity {

    public static final VoxelShape SHAPE = Block.createCuboidShape(
        0, 0, 0, 16, 2, 16
    );

    public BraidDisplayBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        var entity = world.getBlockEntity(pos);
        if (player.isSneaking() && entity instanceof BraidDisplayBlockEntity display && display.display != null) {
            display.display.app.eventBinding.add(new KeyPressEvent(GLFW.GLFW_KEY_I, GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_I), new KeyModifiers(GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL)));

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BraidDisplayBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }
}
