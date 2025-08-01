package io.wispforest.owo.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to fields to define the name of a predicate
 * method to use for verifying values of said field
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PredicateConstraint {
    /**
     * The name of the method used to validate users input
     */
    String inputMethodName() default "";

    /**
     * The name of the method used to validate value being submitted
     */
    String applyMethodName();
}
