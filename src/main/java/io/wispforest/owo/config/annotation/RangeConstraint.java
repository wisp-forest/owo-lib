package io.wispforest.owo.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to fields with a numeric value to express
 * a range of values which should be accepted
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RangeConstraint {
    double min() default -Double.MAX_VALUE;

    double max() default Double.MAX_VALUE;

    /**
     * @return How many decimals places to show in the config
     * screen, if this is a floating point option
     */
    int decimalPlaces() default 2;

    boolean useSlider() default true;
}
