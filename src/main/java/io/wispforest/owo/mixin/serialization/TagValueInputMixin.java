package io.wispforest.owo.mixin.serialization;

import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.util.MapCarrierDecodable;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.KeyedEndecDecodeError;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInputContextHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TagValueInput.class)
public abstract class TagValueInputMixin implements MapCarrierDecodable {
    @Shadow
    @Final
    private CompoundTag input;

    @Shadow
    @Final
    private ProblemReporter problemReporter;

    @Shadow
    @Final
    private ValueInputContextHelper context;

    // TODO: Maybe pass in the ErrorReporter for use within Endecs?
    @Override
    public <T> T getWithErrors(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        ctx = CodecUtils.createContext(this.context.ops(), ctx);

        return this.input.getWithErrors(ctx, key);
    }

    @Override
    public <T> T get(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        try {
            return this.getWithErrors(ctx, key);
        } catch (Exception e) {
            this.problemReporter.report(new KeyedEndecDecodeError(key, this.input.get(key.key()), e));

            return key.defaultValue();
        }
    }
}
