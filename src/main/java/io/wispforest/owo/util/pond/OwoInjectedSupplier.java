package io.wispforest.owo.util.pond;

import java.util.function.Supplier;

public interface OwoInjectedSupplier<T> extends Supplier<T> {
    default T get() {
        throw new IllegalStateException("Injected Supplier interface has yet to be override!");
    }
}
