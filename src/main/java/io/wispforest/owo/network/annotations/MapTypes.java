package io.wispforest.owo.network.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface MapTypes {

    Class<?> keys();

    Class<?> values();

}
