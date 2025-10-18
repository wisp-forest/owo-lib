package io.wispforest.owo.mixin.serialization;

import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.util.MapCarrierDecodable;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.KeyedEndecDecodeError;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadContext;
import net.minecraft.util.ErrorReporter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NbtReadView.class)
public abstract class NbtReadViewMixin implements MapCarrierDecodable {
    @Shadow
    @Final
    private NbtCompound nbt;

    @Shadow
    @Final
    private ErrorReporter reporter;

    @Shadow
    @Final
    private ReadContext context;

    // TODO: Maybe pass in the ErrorReporter for use within Endecs?
    @Override
    public <T> T getWithErrors(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        ctx = CodecUtils.createContext(this.context.getOps(), ctx);

        return this.nbt.getWithErrors(ctx, key);
    }

    @Override
    public <T> T get(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        try {
            return this.getWithErrors(ctx, key);
        } catch (Exception e) {
            this.reporter.report(new KeyedEndecDecodeError(key, this.nbt.get(key.key()), e));

            return key.defaultValue();
        }
    }
}
