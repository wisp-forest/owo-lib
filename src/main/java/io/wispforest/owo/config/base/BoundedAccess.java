package io.wispforest.owo.config.base;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.function.Function;

///
/// An interface used to give access to either an [Object]/[Record] field as
/// an abstraction around either a [Field] or [RecordComponent]
///
public interface BoundedAccess<T> extends AnnotatedElement {

    ///
    /// The name of the field for a given object
    ///
    String name();

    ///
    /// The owner object to which the field is accessed
    ///
    Object owner();

    ///
    /// The [Class<?>] of the given field without any generics
    ///
    Class<T> type();

    ///
    /// The [Type] of the given field with any generics
    ///
    Type genericType();

    @Nullable
    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    ///
    /// Get the current [T] entry from [owner][#owner()]
    ///
    T getValue();

    ///
    /// A simple container which stores both a record component
    /// and an instance of the containing class on which to query
    /// values
    ///
    /// @param owner     The owner object which holds the value the field points to
    /// @param component The component itself
    /// @param <T>       The type of object this field stores
    ///
    record BoundRecordComponent<T>(Record owner, RecordComponent component, Function<Record, T> getter) implements BoundedAccess<T> {
        public String name() {
            return this.component.getName();
        }

        @Override
        public Class<T> type() {
            return (Class<T>) this.component.getType();
        }

        public Type genericType() {
            return this.component.getGenericType();
        }

        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
            return this.component.getAnnotation(annotationClass);
        }

        @Override
        @NotNull
        public Annotation[] getAnnotations() {
            return this.component.getAnnotations();
        }

        @Override
        @NotNull
        public Annotation[] getDeclaredAnnotations() {
            return this.component.getDeclaredAnnotations();
        }

        public T getValue() {
            try {
                return (T) getter.apply(owner);
            } catch (Throwable e) {
                throw new RuntimeException("Could not access config option field " + this.name(), e);
            }
        }

        @ApiStatus.Internal
        public BoundRecordComponent<T> withOwner(Record owner) {
            return new BoundRecordComponent<>(owner, this.component(), this.getter());
        }
    }

    ///
    /// A simple container which stores both a non-static field
    /// and an instance of the containing class on which to query
    /// values
    ///
    /// @param owner The owner object which holds the value the field points to
    /// @param field The field itself
    /// @param <T>   The type of object this field stores
    ///
    @SuppressWarnings("unchecked")
    record BoundField<T>(Object owner, Field field, Class<T> type, Type genericType) implements BoundedAccess<T> {

        public BoundField(Object owner, Field field) {
            this(owner, field, (Class<T>) field.getType(), field.getGenericType());
        }

        public String name() {
            return this.field.getName();
        }

        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
            return this.field.getAnnotation(annotationClass);
        }

        @Override
        @NotNull
        public Annotation[] getAnnotations() {
            return this.field.getAnnotations();
        }

        @Override
        @NotNull
        public Annotation[] getDeclaredAnnotations() {
            return this.field.getDeclaredAnnotations();
        }

        public T getValue() {
            try {
                return (T) this.field.get(this.owner);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access config option field " + this.name(), e);
            }
        }

        public void setValue(T value) {
            try {
                this.field.set(this.owner, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not set config option field " + this.name(), e);
            }
        }

        @ApiStatus.Internal
        public BoundField<T> withOwner(Object owner) {
            return new BoundField<>(owner, this.field(), this.type(), this.genericType());
        }
    }
}
