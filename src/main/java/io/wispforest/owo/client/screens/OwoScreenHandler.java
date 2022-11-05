package io.wispforest.owo.client.screens;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface OwoScreenHandler {
    default <T> SyncedProperty<T> createProperty(Class<T> klass, T initial) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    default <R extends Record> void addServerboundMessage(Class<R> messageClass, Consumer<R> handler) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    default <R extends Record> void addClientboundMessage(Class<R> messageClass, Consumer<R> handler) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    default <R extends Record> void sendMessage(@NotNull R message) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    default PlayerEntity player() {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }
}
