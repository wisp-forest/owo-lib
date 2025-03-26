package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.ConfigWrapper;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public interface ConfigScreenProvider<W extends ConfigWrapper<?>> {

    static <W extends ConfigWrapper<?>, S extends Screen> ConfigScreenProvider<W> of(Class<W> wrapperClass, BiFunction<@Nullable Screen, W, S> supplier) {
        return (screen, wrapper) -> {
            if (!wrapperClass.isInstance(wrapper)) {
                throw new IllegalStateException("Unable to cast the given wrapper [" + wrapper.id() + "] to the required class [" + wrapperClass + "]");
            }

            return supplier.apply(screen, wrapperClass.cast(wrapper));
        };
    }

    Screen openScreenSafely(@Nullable Screen screen, ConfigWrapper<?> wrapper) throws IllegalStateException;
}
