package io.wispforest.owo.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Applied to a field to declare that a method for
 * registering subscribers should be generated
 */
@Target(ElementType.FIELD)
public @interface Hook {}
