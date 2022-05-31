package io.wispforest.owo.mixin;

import io.wispforest.owo.util.Maldenhagen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkSectionCache;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

// welcome to maldenhagen, it moved
// it originally lived in things, but it was malding too hard there
// see Maldenhagen for how this is used
@Mixin(OreFeature.class)
public class Copenhagen {

    // this map contains the seethe'd orr blocks. its quite important
    private final ThreadLocal<Map<BlockPos, BlockState>> OWO$COPING = ThreadLocal.withInitial(HashMap::new);

    // this target method is just so damn complex that not even mixin can correctly guess the injector signature.
    // i just kinda gave up and deleted some of them until it worked. very epic
    //
    // oh also the method caches all the spots that gleaming ore was placed at, so we can later update them for it to glow.
    // of course that needs to be done later, because mojang decided it should. the actual reason is that ChunkSectionCache
    // locks its chunk sections.
    //
    // now you would think this throws an error when you then try to modify those sections. but no.
    // it just silently deadlocks the entire game
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "generateVeinPart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void malding(StructureWorldAccess world, Random random, OreFeatureConfig config, double startX, double endX, double startZ, double endZ,
                         double startY, double endY, int p_x, int p_y, int p_z, int p_horizontalSize, int p_verticalSize, CallbackInfoReturnable<Boolean> cir,
                         int i, BitSet bitSet, BlockPos.Mutable mutable, int j, double[] ds, ChunkSectionCache chunkSectionCache, int m, double d, double e,
                         double g, double h, int n, int o, int p, int q, int r, int s, int t, double u, int v, double w, int aa, ChunkSection chunkSection,
                         int ad, int ae, int af, BlockState blockState, Iterator<OreFeatureConfig.Target> var57, OreFeatureConfig.Target target) {

        if (!Maldenhagen.isOnCopium(target.state.getBlock())) return;
        OWO$COPING.get().put(new BlockPos(t, v, aa), target.state);
    }

    // now in here we read all the gleaming ore spots from our cache and actually cause a block update so that the
    // lighting calculations happen. all of this just so that some dumb orr block can glow.
    @Inject(method = "generateVeinPart", at = @At("TAIL"))
    private void coping(StructureWorldAccess world, Random random, OreFeatureConfig config, double startX, double endX, double startZ, double endZ,
                        double startY, double endY, int x, int y, int z, int horizontalSize, int verticalSize, CallbackInfoReturnable<Boolean> cir) {

        OWO$COPING.get().forEach((blockPos, state) -> {
            world.setBlockState(blockPos, state, Block.NOTIFY_ALL);
        });
        OWO$COPING.get().clear();
    }

}
