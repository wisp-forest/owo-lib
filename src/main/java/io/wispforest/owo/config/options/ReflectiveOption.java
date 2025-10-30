package io.wispforest.owo.config.options;

import io.wispforest.owo.config.base.BoundedAccess;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

///
/// An extension of [OptionControlSpec] useful to gain access to any
/// [BoundedAccess] or [Annotation] information bound to the given option.
///
public sealed interface ReflectiveOption<T> extends OptionControlSpec<T>, AnnotatedElement permits FieldOption, RecordOption {

    ///
    /// @return the [BoundedAccess] for the given option allowing for various
    /// reflective access of the base object and its location within Java
    ///
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
