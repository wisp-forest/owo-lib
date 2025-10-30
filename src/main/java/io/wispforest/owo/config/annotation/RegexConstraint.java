package io.wispforest.owo.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

///
/// Applied to fields which can be represented as a [String]
/// to define a regular expressions all values need to match
///
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegexConstraint {

    ///
    /// @return The Regex used when validating the user input
    ///
    String inputValue() default "";

    ///
    /// @return The Regex used when validating the value when submitting the value
    ///
    String applyValue();
}
