package io.wispforest.owo.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.util.Maldenhagen;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import java.util.HashMap;
import java.util.Map;

@Mixin(OreFeature.class)
public class Copenhagen {
    @Unique private final ThreadLocal<Map<BlockPos, BlockState>> COPING = ThreadLocal.withInitial(HashMap::new);

    // Record the actual placed state and world position. Updating light while
    // BulkSectionAccess still holds its section locks would deadlock worldgen.
    @WrapOperation(method = "doPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState malding(LevelChunkSection section, int x, int y, int z, BlockState state, boolean lock,
                              Operation<BlockState> original, @Local BlockPos.MutableBlockPos position) {
        var result = original.call(section, x, y, z, state, lock);
        if (Maldenhagen.isOnCopium(state.getBlock())) COPING.get().put(position.immutable(), state);
        return result;
    }

    @WrapMethod(method = "doPlace")
    private boolean coping(WorldGenLevel world, RandomSource random, double startX, double endX,
                           double startZ, double endZ, double startY, double endY,
                           int x, int y, int z, int horizontalSize, int verticalSize, Operation<Boolean> original) {
        var positions = COPING.get();
        positions.clear();
        try {
            boolean result = original.call(world, random, startX, endX, startZ, endZ, startY, endY,
                x, y, z, horizontalSize, verticalSize);
            positions.forEach((position, state) -> world.setBlock(position, state, Block.UPDATE_ALL));
            return result;
        } finally {
            positions.clear();
        }
    }
}
