package io.wispforest.uwu.block;

import com.mojang.serialization.MapCodec;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.events.KeyPressEvent;
import io.wispforest.owo.braid.core.events.MouseButtonPressEvent;
import io.wispforest.owo.braid.core.events.MouseButtonReleaseEvent;
import io.wispforest.owo.braid.core.events.MouseMoveEvent;
import io.wispforest.owo.braid.quads.Ray;
import io.wispforest.owo.braid.quads.WorldQuad;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class BraidDisplayBlock extends BlockWithEntity {

    public static final VoxelShape SHAPE = Block.createCuboidShape(
        0, 0, 0, 16, 1, 16
    );

    public BraidDisplayBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        var entity = world.getBlockEntity(pos);
        if (entity instanceof BraidDisplayBlockEntity display) {
            if (display.app == null) return ActionResult.PASS;

            if (player.isSneaking()) {
                display.app.eventBuffer.add(new KeyPressEvent(GLFW.GLFW_KEY_I, GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_I), new KeyModifiers(GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL)));
            } else {
                display.app.eventBuffer.add(new MouseButtonPressEvent(GLFW.GLFW_MOUSE_BUTTON_LEFT, KeyModifiers.NONE));
                display.app.eventBuffer.add(new MouseButtonReleaseEvent(GLFW.GLFW_MOUSE_BUTTON_LEFT, KeyModifiers.NONE));
            }

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
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }
}
