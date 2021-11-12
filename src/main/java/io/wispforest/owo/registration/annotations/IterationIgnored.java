package io.wispforest.owo.registration.annotations;

import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the target field should be ignored by all operations
 * of {@link FieldRegistrationHandler}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IterationIgnored {}
