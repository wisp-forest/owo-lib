package io.wispforest.owo.network.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(java.lang.annotation.ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface ElementType {

    Class<?> value();

}
