package io.wispforest.owo.serialization.format;

import com.mojang.serialization.DynamicOps;
import io.wispforest.endec.SerializationContext;
import net.minecraft.util.dynamic.ForwardingDynamicOps;

public class DynamicOpsWithContext<T> extends ForwardingDynamicOps<T> implements ContextHolder {

    private final SerializationContext capturedContext;

    protected DynamicOpsWithContext(SerializationContext capturedContext, DynamicOps<T> delegate) {
        super(delegate);

        this.capturedContext = capturedContext;
    }

    public static <T> DynamicOpsWithContext<T> of(SerializationContext context, DynamicOps<T> delegate) {
        return new DynamicOpsWithContext<>(context, delegate);
    }

    public static <T> DynamicOpsWithContext<T> ofEmptyContext(DynamicOps<T> delegate) {
        return new DynamicOpsWithContext<>(SerializationContext.empty(), delegate);
    }

    @Override
    public SerializationContext capturedContext() {
        return this.capturedContext;
    }
}
