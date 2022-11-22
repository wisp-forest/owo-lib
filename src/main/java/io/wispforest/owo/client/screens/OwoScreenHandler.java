package io.wispforest.owo.client.screens;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface OwoScreenHandler {

    /**
     * Create a new property on this screen handler. This property can be updated serverside
     * and will automatically synchronize to the client - think {@link net.minecraft.screen.PropertyDelegate}
     * but without being restricted to integers
     *
     * @param klass   The class of the property's value
     * @param initial The value with which to initialize the property
     * @return The created property
     */
    default <T> SyncedProperty<T> createProperty(Class<T> klass, T initial) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    /**
     * Register a serverbound message, or local packet if you will, onto this
     * screen handler. This needs to be called during initialization of the handler,
     * after which you can send messages to the server by invoking {@link #sendMessage(Record)}
     * with the message you want to send
     *
     * @param messageClass The class of message to send, must be a record - much like
     *                     packets in an {@link io.wispforest.owo.network.OwoNetChannel}
     * @param handler      The handler to execute when a message of the given class is
     *                     received on the server
     */
    default <R extends Record> void addServerboundMessage(Class<R> messageClass, Consumer<R> handler) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    /**
     * Register a clientbound message, or local packet if you will, onto this
     * screen handler. This needs to be called during initialization of the handler,
     * after which you can send messages to the client by invoking {@link #sendMessage(Record)}
     * with the message you want to send
     *
     * @param messageClass The class of message to send, must be a record - much like
     *                     packets in an {@link io.wispforest.owo.network.OwoNetChannel}
     * @param handler      The handler to execute when a message of the given class is
     *                     received on the client
     */
    default <R extends Record> void addClientboundMessage(Class<R> messageClass, Consumer<R> handler) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    /**
     * Send the given message. This message must have been previously
     * registered through a call to {@link #addServerboundMessage(Class, Consumer)}
     * or {@link #addClientboundMessage(Class, Consumer)} - this also dictates where
     * the message will be sent to
     */
    default <R extends Record> void sendMessage(@NotNull R message) {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }

    /**
     * @return The player this screen handler is attached to
     */
    default PlayerEntity player() {
        throw new IllegalStateException("Implemented in ScreenHandlerMixin");
    }
}
