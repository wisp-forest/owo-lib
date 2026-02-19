package io.wispforest.owo.braid.framework;

import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import org.jetbrains.annotations.Nullable;

public interface BuildContext {
    <T> @Nullable T getAncestor(Class<T> ancestorClass, Object inheritedKey);

    default <T> @Nullable T getAncestor(Class<T> ancestorClass) {
        return this.getAncestor(ancestorClass, ancestorClass);
    }

    <T> @Nullable T dependOnAncestor(Class<T> ancestorClass, Object inheritedKey, @Nullable Object dependency);

    default <T> @Nullable T dependOnAncestor(Class<T> ancestorClass, Object inheritedKey) {
        return this.dependOnAncestor(ancestorClass, inheritedKey, null);
    }

    default <T> @Nullable T dependOnAncestor(Class<T> ancestorClass) {
        return this.dependOnAncestor(ancestorClass, ancestorClass);
    }

    /// To prevent excessive IDE warnings, the return type of this
    /// getter is not annotated `@Nullable` even though if it is called
    /// before this context has been laid out, it will (correctly)
    /// return null
    WidgetInstance<?> instance();
}
