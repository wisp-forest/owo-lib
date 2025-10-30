package io.wispforest.owo.config.annotation;

import io.wispforest.owo.config.ConfigWrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

///
/// Applied to a class to mark is as a config model. This means an
/// implementation of [ConfigWrapper]
/// will be generated which can subsequently be used to manage
/// the config data described by the annotated class
///
/// @see ConfigWrapper
///
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {
    ///
    /// @return The name of the wrapper class to generate
    ///
    String wrapperName();

    ///
    /// @return The name under which to save the config
    ///
    String name();

    ///
    /// @return The id for the mod this config pertains to
    ///
    String modId();

    ///
    /// @return `true` if all fields should be treated
    /// as if they were annotated with [Hook]
    ///
    boolean defaultHook() default false;

    ///
    /// @return `true` if this config should automatically
    /// be saved whenever it is modified
    ///
    boolean saveOnModification() default true;
}
