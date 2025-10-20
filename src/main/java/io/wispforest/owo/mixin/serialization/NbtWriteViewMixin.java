package io.wispforest.owo.mixin.serialization;

import com.mojang.serialization.DynamicOps;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.util.MapCarrierEncodable;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.KeyedEndecEncodeError;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NbtWriteView.class)
public abstract class NbtWriteViewMixin implements MapCarrierEncodable {
    @Shadow
    @Final
    private NbtCompound nbt;

    @Shadow
    @Final
    private ErrorReporter reporter;

    @Shadow
    @Final
    private DynamicOps<NbtElement> ops;

    @Override
    public <T> void put(SerializationContext ctx, @NotNull KeyedEndec<T> key, @NotNull T value) {
        ctx = CodecUtils.createContext(this.ops, ctx);

        try {
            this.nbt.put(ctx, key, value);
        } catch (Exception e) {
            boolean defaultValueErrored = false;

            // TODO: Unknow if such is best to encode default value as KeyedEndec have a default value getter
//            try {
//                this.nbt.put(ctx, key, key.defaultValue());
//            } catch (Exception ignore) {
//                defaultValueErrored = true;
//            }

            reporter.report(new KeyedEndecEncodeError(key, value, e, !defaultValueErrored));
        }
    }
}
