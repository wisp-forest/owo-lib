package io.wispforest.owo.config.annotation;

import io.wispforest.owo.config.Option;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to a field to indicate that
 * its value should be synchronized between server
 * and client in some way
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Sync {
    Option.SyncMode value();
}
