package io.wispforest.owo.client.screens;

public interface OwoScreenHandler {
    default <T> SyncedProperty<T> addProperty(Class<T> klass, T initial) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }
}
