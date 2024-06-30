package io.wispforest.owo.client.screens;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface OwoScreenHandler {

    default ReflectiveEndecBuilder endecBuilder() {
        throw new UnsupportedOperationException("Implemented in ScreenHandlerMixin");
    }

    /**
     * Create a new property on this screen handler. This property can be updated serverside
     * and will automatically synchronize to the client - think {@link net.minecraft.screen.PropertyDelegate}
     * but without being restricted to integers
     *
     * @param clazz   The class of the property's value
     * @param endec   The endec to use for (de-)serializing the value of this property over the network
     * @param initial The value with which to initialize the property
     * @return The created property
     */
    default <T> SyncedProperty<T> createProperty(Class<T> clazz, Endec<T> endec, T initial) {
        throw new UnsupportedOperationException("Implemented in ScreenHandlerMixin");
    }

    /**
     * Shorthand for {@link #createProperty(Class, Endec, Object)} which creates the endec
     * through {@link ReflectiveEndecBuilder#get(Class)}
     */
    default <T> SyncedProperty<T> createProperty(Class<T> clazz, T initial) {
        return this.createProperty(clazz, this.endecBuilder().get(clazz), initial);
    }

    /**
     * Register a serverbound message, or local packet if you will, onto this
     * screen handler. This needs to be called during initialization of the handler,
     * after which you can send messages to the server by invoking {@link #sendMessage(Record)}
     * with the message you want to send
     *
     * @param messageClass The class of message to send, must be a record - much like
     *                     packets in an {@link io.wispforest.owo.network.OwoNetChannel}
     * @param endec        The endec to use for (de-)serializing messages sent over the network
     * @param handler      The handler to execute when a message of the given class is
     *                     received on the server
     */
    default <R extends Record> void addServerboundMessage(Class<R> messageClass, Endec<R> endec, Consumer<R> handler) {
        throw new UnsupportedOperationException("Implemented in ScreenHandlerMixin");
    }

    /**
     * Shorthand for {@link #addServerboundMessage(Class, Endec, Consumer)} which creates the endec
     * through {@link ReflectiveEndecBuilder#get(Class)}
     */
    default <R extends Record> void addServerboundMessage(Class<R> messageClass, Consumer<R> handler) {
        this.addServerboundMessage(messageClass, this.endecBuilder().get(messageClass), handler);
    }

    /**
     * Register a clientbound message, or local packet if you will, onto this
     * screen handler. This needs to be called during initialization of the handler,
     * after which you can send messages to the client by invoking {@link #sendMessage(Record)}
     * with the message you want to send
     *
     * @param messageClass The class of message to send, must be a record - much like
     *                     packets in an {@link io.wispforest.owo.network.OwoNetChannel}
     * @param endec        The endec to use for (de-)serializing messages sent over the network
     * @param handler      The handler to execute when a message of the given class is
     *                     received on the client
     */
    default <R extends Record> void addClientboundMessage(Class<R> messageClass, Endec<R> endec, Consumer<R> handler) {
        throw new UnsupportedOperationException("Implemented in ScreenHandlerMixin");
    }

    /**
     * Shorthand for {@link #addClientboundMessage(Class, Endec, Consumer)} which creates the endec
     * through {@link ReflectiveEndecBuilder#get(Class)}
     */
    default <R extends Record> void addClientboundMessage(Class<R> messageClass, Consumer<R> handler) {
        this.addClientboundMessage(messageClass, this.endecBuilder().get(messageClass), handler);
    }

    /**
     * Send the given message. This message must have been previously
     * registered through a call to {@link #addServerboundMessage(Class, Endec, Consumer)}
     * or {@link #addClientboundMessage(Class, Endec, Consumer)} - this also dictates where
     * the message will be sent to
     */
    default <R extends Record> void sendMessage(@NotNull R message) {
        throw new UnsupportedOperationException("Implemented in ScreenHandlerMixin");
    }

    /**
     * @return The player this screen handler is attached to
     */
    default PlayerEntity player() {
        throw new UnsupportedOperationException("Implemented in ScreenHandlerMixin");
    }
}
