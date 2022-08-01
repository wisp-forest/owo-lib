package io.wispforest.owo.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to a field to indicate that
 * the generated screen should prepend
 * a section header to option
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SectionHeader {
    /**
     * @return The name of the section describe by this annotation. Used to
     * derive a translation key with the pattern {@code text.config.<config name>.section.<name>}
     */
    String value();
}
