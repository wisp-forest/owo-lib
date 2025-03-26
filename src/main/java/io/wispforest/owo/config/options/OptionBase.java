package io.wispforest.owo.config.options;

import io.wispforest.owo.Owo;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.base.Key;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public sealed abstract class OptionBase<T> implements OptionControlSpec<T> permits FieldOption, MemoryOption {

    private final Identifier configId;
    private final Key key;
    private final String translationKey;

    private final T defaultValue;

    private final Class<T> clazz;
    private final Type genericType;

    private final ConfigWrapper.@Nullable Constraint constraint;

    /**
     * @param configId   The name of the config this option is contained in
     * @param key          The key of this option
     * @param defaultValue The default value of this option
     *                     emitting events when it changes as well as correcting
     *                     invalid values after deserialization
     * @param constraint   The constraint placed on the value of this option,
     *                     or {@code null} if the option is unconstrained
     */
    @SuppressWarnings("unchecked")
    public OptionBase(Identifier configId,
                             Key key,
                             T defaultValue,
                             Class<T> clazz,
                             Type genericType,
                             @Nullable ConfigWrapper.Constraint constraint
    ) {
        this.configId = configId;
        this.key = key;
        this.translationKey = "text.config." + this.configId.getPath() + ".option." + this.key.asString();

        this.defaultValue = defaultValue;

        this.clazz = clazz;
        this.genericType = genericType;

        this.constraint = constraint;
    }

    @Override
    public T defaultValue() {
        return this.defaultValue;
    }

    @Override
    public abstract T value();

    @Override
    public abstract void set(T t);

    @Override
    public Class<T> clazz() {
        return this.clazz;
    }

    @Override
    public Type getGenericType() {
        return this.genericType;
    }

    @Override
    public boolean verifyConstraint(T value) {
        if (this.constraint == null) return true;

        final var matched = this.constraint.test(value);
        if (!matched) {
            Owo.LOGGER.warn(
                    "Option {} in config '{}' could not be updated, as the given value '{}' does not match its constraint: {}",
                    this.key, this.configId, value, this.constraint.formatted()
            );
        }

        return matched;
    }

    @Override
    public String translationKey() {
        return this.translationKey;
    }

    @Override
    public Identifier configId() {
        return this.configId;
    }

    @Override
    public Key key() {
        return this.key;
    }

    @Override
    public @Nullable ConfigWrapper.Constraint constraint() {
        return this.constraint;
    }

    @Override
    public String toString() {
        return "Option[" +
                "configId=" + configId + ", " +
                "key=" + key + ", " +
                "defaultValue=" + defaultValue + ", " +
                "constraint=" + (constraint == null ? null : constraint.formatted())
                + "]";
    }
}
