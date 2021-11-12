package io.wispforest.owo.registration.annotations;

import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used to override the namespace an implementation of
 * {@link AutoRegistryContainer} uses.
 * <p>
 * This only applies to inner classes, top level classes have their namespace defined
 * in the call to {@link FieldRegistrationHandler}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegistryNamespace {
    String value();
}
