package io.wispforest.owo.neoforge.mixin.text;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(TranslatableContents.class)
public class TranslatableContentsMixin {

    @Shadow
    @Final
    private Object[] args;

    @Inject(method = "<init>", at = @At("HEAD"))
    private static void setupShare(String key, String fallback, Object[] args, CallbackInfo ci, @Share(value = "", namespace = "owo") LocalRef<IntSet> toBeConvertedIndices){
        toBeConvertedIndices.set(new IntOpenHashSet());
    }
    
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/contents/TranslatableContents;isAllowedPrimitiveArgument(Ljava/lang/Object;)Z"))
    private boolean allowIdentifiers(Object object, Operation<Boolean> original,
                                     @Local(ordinal = 1) int index,
                                     @Share(value = "", namespace = "owo") LocalRef<IntSet> toBeConvertedIndices) {
        if (!(object instanceof Identifier)) return original.call(object);
        toBeConvertedIndices.get().add(index);
        return true;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void handleIdentifiers(String key, String fallback, Object[] args, CallbackInfo ci, @Share(value = "", namespace = "owo") LocalRef<IntSet> toBeConvertedIndices){
        var itr = toBeConvertedIndices.get().iterator();

        while (itr.hasNext()) {
            int index = itr.nextInt();
            this.args[index] = this.args[index].toString();
        }
    }
}
