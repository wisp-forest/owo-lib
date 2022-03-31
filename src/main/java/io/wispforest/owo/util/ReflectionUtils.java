package io.wispforest.owo.util;

import io.wispforest.owo.registration.annotations.AssignedName;
import io.wispforest.owo.registration.annotations.IterationIgnored;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Experimental
public class ReflectionUtils {
    /**
     * Tries to instantiate the given class with a zero-args constructor call,
     * throws a {@link RuntimeException} if it fails
     *
     * @param clazz The class to instantiate
     * @param <C>   The type of object that results
     * @return The created instance of <b>C</b>
     */
    public static <C> C tryInstantiateWithNoArgs(Class<C> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException((e instanceof NoSuchMethodException ? "No zero-args constructor defined on class " : "Could not instantiate class ") + clazz, e);
        }
    }

    /**
     * Calls the {@link Constructor#newInstance(Object...)} method and
     * wraps the exception in a {@link RuntimeException}, thus making it unchecked.
     * <b>Use this when you would otherwise rethrow</b>
     *
     * @param constructor The constructor to call
     * @param args        The arguments to pass the constructor
     * @param <C>         The type of object to create
     * @return The created object
     */
    public static <C> C instantiate(Constructor<C> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Wrapped object creation failure, look below for reason", e);
        }
    }

    /**
     * Tries to obtain the public zero-args constructor of the given class.
     * <b>Use this when no constructor constitutes an error condition or
     * you previously checked for its existence with {@link #requireZeroArgsConstructor(Class, Function)}</b>
     *
     * @param clazz The class to get the constructor from
     * @param <C>   The type of object the constructor will create
     * @return The public zero-args constructor of the given class
     */
    public static <C> Constructor<C> getNoArgsConstructor(Class<C> clazz) {
        try {
            return clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Class " + clazz.getName() + " does not declare a zero-args constructor", e);
        }
    }

    /**
     * Iterates all accessible static fields of the given class and
     * calls the field consumer on each applicable one
     *
     * @param clazz           The target class
     * @param targetFieldType The field type match
     * @param fieldConsumer   The function to apply to each field, supplied
     *                        with the field's value and ID
     * @param <C>             The type of {@code clazz}
     * @param <F>             The type of field to match
     */
    @SuppressWarnings("unchecked")
    public static <C, F> void iterateAccessibleStaticFields(Class<C> clazz, Class<F> targetFieldType, FieldConsumer<F> fieldConsumer) {
        for (var field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;

            F value;
            try {
                value = (F) field.get(null);
            } catch (IllegalAccessException e) {
                continue;
            }

            if (value == null || !targetFieldType.isAssignableFrom(value.getClass())) continue;
            if (field.isAnnotationPresent(IterationIgnored.class)) continue;

            fieldConsumer.accept(value, getFieldName(field), field);
        }
    }

    /**
     * Returns the name of field in all lowercase, or
     * the name defined by an {@link AssignedName} annotation
     *
     * @param field The field to check
     * @return the properly formatted field name
     */
    public static String getFieldName(Field field) {
        var fieldId = field.getName().toLowerCase(Locale.ROOT);
        if (field.isAnnotationPresent(AssignedName.class)) fieldId = field.getAnnotation(AssignedName.class).value();
        return fieldId;
    }

    /**
     * Executes the given consumer on all subclasses that match {@code targetType}
     *
     * @param parent     The parent class
     * @param targetType The subclass type to match
     * @param action     The action to execute on each subclass
     */
    public static void forApplicableSubclasses(Class<?> parent, Class<?> targetType, Consumer<Class<?>> action) {
        for (var subclass : parent.getDeclaredClasses()) {
            if (!targetType.isAssignableFrom(subclass)) continue;
            action.accept(subclass);
        }
    }

    /**
     * Verifies that the given class provides a public zero-args constructor.
     * Throws an exception with a caller-controlled message if the constructor
     * doesn't exist
     *
     * @param clazz           The class to check the existence of a zero-args constructor for
     * @param reasonFormatter The error message to throw, gets the class name passed
     */
    public static void requireZeroArgsConstructor(Class<?> clazz, Function<String, String> reasonFormatter) {
        boolean found = false;
        for (var constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() != 0) continue;
            found = true;
            break;
        }

        if (!found) throw new IllegalStateException(reasonFormatter.apply(clazz.getName()));
    }

    /**
     * Tries to acquire the name of the calling class,
     * {@code depth} frames up the call stack
     *
     * @param depth How many frames upwards to walk the call stack
     * @return The name of the class at {@code depth} in the call stack or
     * {@code <unknown>} if the class name was not found
     */
    public static String getCallingClassName(int depth) {
        return StackWalker.getInstance().walk(s -> s
            .skip(depth)
            .map(StackWalker.StackFrame::getClassName)
            .findFirst()).orElse("<unknown>");
    }

    @FunctionalInterface
    public interface FieldConsumer<F> {
        void accept(F value, String name, Field field);
    }
}
