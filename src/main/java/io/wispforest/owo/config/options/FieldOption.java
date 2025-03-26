package io.wispforest.owo.config.options;

import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.annotation.RestartRequired;
import io.wispforest.endec.Endec;
import io.wispforest.owo.config.base.BoundedAccess;
import io.wispforest.owo.config.base.Key;
import io.wispforest.owo.config.base.SyncMode;
import io.wispforest.owo.util.Observable;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Describes a single option in a config. Instances
 * of this class keep a reference to the field in
 * the model class which stores the value used for serialization.
 * <p>
 * An option may enter the so-called "detached" state, which means
 * its value is being overridden by the server. In this state, the option
 * is completely immutable and can only be changed again afterwards
 */
public final class FieldOption<T> extends OptionBase<T> implements ReflectiveOption<T> {

    private final BoundedAccess.BoundField<T> backingField;

    private final Observable<T> mirror;

    private final @Nullable Endec<T> endec;
    private final SyncMode syncMode;

    /**
     * Indicates whether this option is currently being overridden
     * by the server and should thus never synchronize with its backing
     * field and behave immutably to the client
     */
    private boolean detached = false;

    /**
     * @param configName   The name of the config this option is contained in
     * @param key          The key of this option
     * @param defaultValue The default value of this option
     * @param mirror       A mirror of the value of this option, used for
     *                     emitting events when it changes as well as correcting
     *                     invalid values after deserialization
     * @param backingField The backing field in the config model class
     *                     which this option describes
     * @param constraint   The constraint placed on the value of this option,
     *                     or {@code null} if the option is unconstrained
     */
    @SuppressWarnings("unchecked")
    public FieldOption(Identifier configId,
                       Key key,
                       T defaultValue,
                       Observable<T> mirror,
                       BoundedAccess.BoundField<T> backingField,
                       @Nullable ConfigWrapper.Constraint constraint,
                       SyncMode syncMode,
                       ReflectiveEndecBuilder builder
    ) {
        super(configId, key, defaultValue, (Class<T>) backingField.type(), backingField.genericType(), constraint);

        this.backingField = backingField;

        this.mirror = mirror;

        this.syncMode = syncMode;
        this.endec = syncMode.isNone() ? null : (Endec<T>) builder.get(this.getGenericType());
    }

    @Override
    public BoundedAccess<T> backingAccess() {
        return this.backingField;
    }

    /**
     * Update the current value of this option,
     * or do nothing if the given value is invalid
     *
     * @param value The new value of the option
     */
    public void set(T value) {
        if (this.detached) return;

        if (!this.verifyConstraint(value)) return;

        // Technically safe cast since constructor requires BoundField type
        this.backingField.setValue(value);
        this.mirror.set(value);
    }

    @Override
    public T value() {
        return this.mirror.get();
    }

    /**
     * Synchronize the value stored in the backing field
     * and this option's mirror - used for either correcting an
     * invalid value after updating the field or updating the mirror
     */
    public void synchronizeWithBackingField() {
        if (this.detached) return;

        final var fieldValue = this.backingField.getValue();
        if (verifyConstraint(fieldValue)) {
            this.mirror.set(fieldValue);
        } else {
            // Technically safe cast since constructor requires BoundField type
            this.backingField.setValue(this.mirror.get());
        }
    }

    /**
     * Add an observer function to be run every time
     * the value of this option changes
     */
    public void observe(Consumer<T> observer) {
        this.mirror.observe(observer);
    }

    /**
     * Write the current value of this option into the given buffer
     *
     * @param buf The packet buffer to write to
     */
    public void write(PacketByteBuf buf) {
        buf.write(this.endec, this.value());
    }

    /**
     * Read a new value of this option from the given buffer
     * and enter a detached state
     *
     * @param buf The packet buffer to read from
     * @return {@code null} if this option was successfully detached,
     * the server's value otherwise
     */
    public T read(PacketByteBuf buf) {
        final var newValue = buf.read(this.endec);

        if (!Objects.equals(newValue, this.value()) && this.backingField.hasAnnotation(RestartRequired.class)) {
            return newValue;
        }

        this.mirror.set(newValue);
        this.detached = true;

        return null;
    }

    /**
     * @return The serializer for this option's value
     */
    public Endec<T> endec() {
        return this.endec;
    }

    /**
     * Reset this option's attached state and synchronize
     * it with the backing field again
     */
    public void reattach() {
        if (!this.detached) return;

        this.detached = false;
        this.synchronizeWithBackingField();
    }

    /**
     * @return {@code true} if this option is currently detached
     */
    public boolean detached() {
        return this.detached;
    }

    /**
     * @return The way in which this option
     * should be synchronized between sever and client
     */
    public SyncMode syncMode() {
        return this.syncMode;
    }
}
