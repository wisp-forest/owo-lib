package io.wispforest.owo.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to a class also annotated with {@link Config}
 * to indicate that a standard owo-config screen should
 * automatically be provided to <a href="https://modrinth.com/mod/modmenu">ModMenu</a>.
 * <p>
 * In case you want more specific control over the generated
 * screen, potentially with a special subclass, you should instead
 * implement {@link com.terraformersmc.modmenu.api.ModMenuApi} like usual
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Modmenu {

    /**
     * @return The mod ID for which to register
     * the config screen factory
     */
    String modId();

    /**
     * @return The ID of the UI model to use for the screen.
     * You can change this to a model you provide in your
     * mod's resources to customize the generated screen
     */
    String uiModelId() default "owo:config";

}
