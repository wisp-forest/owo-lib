package io.wispforest.owo.config.options;

import io.wispforest.owo.config.base.BoundedAccess;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public sealed interface ReflectiveOption<T> extends OptionControlSpec<T>, AnnotatedElement permits FieldOption, RecordOption {

    BoundedAccess<T> backingAccess();

    @Override
    @NotNull
    default Annotation[] getAnnotations() {
        return backingAccess().getAnnotations();
    }

    @Override
    @NotNull
    default Annotation[] getDeclaredAnnotations() {
        return backingAccess().getDeclaredAnnotations();
    }

    @Override
    default <A extends Annotation> A getAnnotation(@NotNull Class<A> annotationClass) {
        return backingAccess().getAnnotation(annotationClass);
    }

    default boolean detached() {
        return false;
    }
}
