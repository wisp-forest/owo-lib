package io.wispforest.owo.text;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiConsumer;

@ApiStatus.Internal
public class LanguageAccess {
    public static final BiConsumer<String, Component> EMPTY_CONSUMER = (string, component) -> {};

    public static ThreadLocal<BiConsumer<String, Component>> textConsumer = ThreadLocal.withInitial(() -> EMPTY_CONSUMER);
}
