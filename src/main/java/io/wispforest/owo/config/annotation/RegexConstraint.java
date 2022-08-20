package io.wispforest.owo.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to fields which can be represented as a {@link CharSequence}
 * to define a regular expressions all values need to match
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegexConstraint {

    String value();

}
