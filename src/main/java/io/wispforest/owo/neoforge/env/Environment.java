package io.wispforest.owo.neoforge.env;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // Needs to be visible to the transformer
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Environment {
    EnvType value();
}
