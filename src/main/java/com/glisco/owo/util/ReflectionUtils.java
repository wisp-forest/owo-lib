package com.glisco.owo.util;

import com.glisco.owo.registration.annotations.FriendlyName;
import com.glisco.owo.registration.annotations.IterationIgnored;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException((e instanceof NoSuchMethodException ? "No zero-args constructor defined on class " : "Could not instantiate class ") + clazz, e);
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
    public static <C, F> void iterateAccessibleStaticFields(Class<C> clazz, Class<F> targetFieldType, BiConsumer<F, String> fieldConsumer) {
        for (var field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;

            F value;
            try {
                value = (F) field.get(null);
            } catch (IllegalAccessException e) {
                continue;
            }

            if (!targetFieldType.isAssignableFrom(value.getClass())) continue;
            if (field.isAnnotationPresent(IterationIgnored.class)) continue;

            var fieldId = field.getName().toLowerCase();
            if (field.isAnnotationPresent(FriendlyName.class)) fieldId = field.getAnnotation(FriendlyName.class).value();

            fieldConsumer.accept(value, fieldId);
        }
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
}
