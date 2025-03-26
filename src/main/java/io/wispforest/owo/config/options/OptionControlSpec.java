package io.wispforest.owo.config.options;

import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.base.Key;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public sealed interface OptionControlSpec<T> permits OptionBase, ReflectiveOption {

    /**
     * @return The default value of this option
     */
    T defaultValue();

    /**
     * @return The current value of this option
     */
    T value();

    void set(T t);

    /**
     * @return The class of this option's value
     */
    Class<T> clazz();

    Type getGenericType();

    /**
     * Check whether the given value passes the constraint
     * of this option and emit a warning if it does not
     *
     * @param value The value to test
     * @return {@code true} if either the given value
     * passes the constraint put on this option or this
     * option is unconstrained
     */
    boolean verifyConstraint(T value);

    //--

    /**
     * @return The translation key of this option
     */
    String translationKey();

    /**
     * @return The id of the config this option is contained in
     */
    Identifier configId();

    /**
     * @return The name of the config this option is contained in
     */
    default String configName() {
        return this.configId().getPath();
    }

    /**
     * @return The key of this option
     */
    Key key();

    /**
     * @return The constraint placed on the value of this option,
     * or {@code null} if the option is unconstrained
     */
    ConfigWrapper.@Nullable Constraint constraint();
}
