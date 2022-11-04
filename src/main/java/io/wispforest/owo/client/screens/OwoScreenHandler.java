package io.wispforest.owo.client.screens;

import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Consumer;

public interface OwoScreenHandler {
    default <T> SyncedProperty<T> addProperty(Class<T> klass, T initial) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    default <R extends Record> void addServerboundPacket(Class<R> klass, Consumer<R> handler) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    default <R extends Record> void addClientboundPacket(Class<R> klass, Consumer<R> handler) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    default <R extends Record> void sendPacket(R packet) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    default PlayerEntity player() {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }
}
