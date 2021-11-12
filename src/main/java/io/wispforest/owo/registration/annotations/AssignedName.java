package io.wispforest.owo.registration.annotations;

import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the name the targeted field should be assigned when processed by
 * {@link FieldRegistrationHandler}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AssignedName {
    String value();
}
