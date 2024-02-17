package io.wispforest.owo.network.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated Moved to {@link io.wispforest.owo.serialization.annotations.SealedPolymorphic} for consistency. This
 * annotation will keep working for the time being but eventually get removed
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated(forRemoval = true)
public @interface SealedPolymorphic {}
