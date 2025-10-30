package io.wispforest.owo.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.wispforest.owo.config.annotation.PredicateConstraint;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.RegexConstraint;
import io.wispforest.owo.config.base.BoundedAccess;
import io.wispforest.owo.config.base.OptionConstraint;
import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.options.ReflectiveOption;
import io.wispforest.owo.util.NumberReflection;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ConfigReflectionUtils {

    public static RangeConstraintData getRangeConstraintData(Class<? extends Number> clazz, OptionControlSpec<?> option) {
        @Nullable BoundedAccess<?> possibleAccess = option instanceof ReflectiveOption<?> reflectiveOption
                ? reflectiveOption.backingAccess()
                : null;

        return getRangeConstraintData(clazz, possibleAccess);
    }

    public static RangeConstraintData getRangeConstraintData(Class<? extends Number> clazz, ReflectiveOption<?> option) {
        return getRangeConstraintData(clazz, option.backingAccess());
    }

    public static RangeConstraintData getRangeConstraintData(Class<? extends Number> clazz, @Nullable BoundedAccess<?> access) {
        var floatingPointType = NumberReflection.isFloatingPointType(clazz);

        var useSlider = false;

        var decimalPlaces = floatingPointType ? 2 : 0;

        double min = NumberReflection.minValue(clazz).doubleValue(),
            max = NumberReflection.maxValue(clazz).doubleValue();

        if (access != null && access.isAnnotationPresent(RangeConstraint.class)) {
            var constraintData = access.getAnnotation(RangeConstraint.class);

            useSlider = constraintData.useSlider();

            if (floatingPointType) decimalPlaces = constraintData.decimalPlaces();

            min = constraintData.min();
            max = constraintData.max();
        }

        return new RangeConstraintData(min, max, decimalPlaces, useSlider);
    }

    @Nullable
    public static <T> OptionConstraint<T> getConstraint(BoundedAccess<T> access) throws IllegalAccessException, NoSuchMethodException {
        var fieldType = access.type();

        OptionConstraint<T> constraint = null;

        if (access.isAnnotationPresent(RangeConstraint.class)) {
            var annotation = access.getAnnotation(RangeConstraint.class);

            if (!NumberReflection.isNumberType(fieldType)) {
                throw new IllegalStateException("@RangeConstraint can only be applied to numeric fields");
            }

            Double min = annotation.min(), max = annotation.max();

            Predicate predicate = (fieldType == long.class || fieldType == Long.class)
                ? o -> o != null && (Long) o >= min && (Long) o <= max
                : o -> o != null && ((Number) o).doubleValue() >= min && ((Number) o).doubleValue() <= max;

            constraint = new OptionConstraint<>("Range from " + min + " to " + max, predicate);
        } else if (access.isAnnotationPresent(RegexConstraint.class)) {
            var annotation = access.getAnnotation(RegexConstraint.class);

            if (!CharSequence.class.isAssignableFrom(fieldType)) {
                throw new IllegalStateException("@RegexConstraint can only be applied to fields with a string representation");
            }

            var applyStr = annotation.applyValue();
            var applyPattern = Pattern.compile(applyStr);
            Predicate applyPrediacate = o -> o != null && applyPattern.matcher((CharSequence) o).matches();

            var inputStr = annotation.inputValue();
            Predicate<String> inputPredicate;

            var format = "Regex [Apply: " + applyStr;

            if (!inputStr.isEmpty()) {
                var inputPattern = Pattern.compile(inputStr);

                format += ", Input: " + inputStr;

                inputPredicate = o -> o != null && inputPattern.matcher(o).matches();
            } else {
                inputPredicate = o -> true;
            }

            format += "]";

            constraint = new OptionConstraint<>(format, applyPrediacate, inputPredicate);
        } else if (access.isAnnotationPresent(PredicateConstraint.class)) {
            var annotation = access.getAnnotation(PredicateConstraint.class);

            var applyMethodName = annotation.applyMethodName();
            Predicate applyPrediacate = getPredicate(access.owner().getClass(), applyMethodName, fieldType);

            var inputMethodName = annotation.inputMethodName();
            Predicate<String> inputPredicate;

            var format = "Predicate method [Apply: " + applyMethodName;

            if (!inputMethodName.isEmpty()) {
                format += ", Input: " + inputMethodName;

                inputPredicate = getPredicate(access.owner().getClass(), inputMethodName, String.class);
            } else {
                inputPredicate = s -> true;
            }

            format += "]";

            constraint = new OptionConstraint<>(format, applyPrediacate, inputPredicate);
        }

        return constraint;
    }

    private static <T> Predicate<T> getPredicate(Class<?> ownerClass, String methodName, Class<T> fieldType) throws IllegalAccessException, NoSuchMethodException {
        Method method = null;

        for (var possbileMethod : ownerClass.getDeclaredMethods()) {
            if (!possbileMethod.getName().equals(methodName)) continue;

            if (possbileMethod.getReturnType() != boolean.class) {
                throw new NoSuchMethodException("Return type of predicate implementation '" + methodName + "' must be 'boolean'");
            } else if (!Modifier.isStatic(possbileMethod.getModifiers())) {
                throw new IllegalStateException("Predicate implementation '" + methodName + "' must be static");
            } else if (!Modifier.isPublic(possbileMethod.getModifiers())) {
                throw new IllegalStateException("Predicate implementation '" + methodName + "' must be public");
            } else if (possbileMethod.getParameterCount() != 1) {
                throw new IllegalStateException("Predicate implementation '" + methodName + "' must have a single parameter");
            }

            var arg = possbileMethod.getParameterTypes()[0];

            if (!fieldType.isAssignableFrom(arg)) {
                throw new IllegalStateException("Predicate implementation '" + methodName + "' parameter must have the given type '" + fieldType.getSimpleName() + "'");
            }

            method = possbileMethod;

            break;
        }

        if (method == null) {
            throw new IllegalStateException("Predicate implementation with the method name '" + methodName + "' dose not exist within '" + ownerClass.getSimpleName() + "'");
        }

        var handle = MethodHandles.publicLookup().unreflect(method);

        return o -> invokePredicate(handle, o);
    }

    private static boolean invokePredicate(MethodHandle predicate, Object value) {
        try {
            return (boolean) predicate.invoke(value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not invoke predicate", e);
        }
    }

    @Nullable
    public static ConfigReflectionUtils.CollectionType getMapType(Type genericType) {
        var keyType = ReflectionUtils.getTypeArgument(genericType, 0);
        var valueTypeData = ReflectionUtils.getTypeAndClassArgument(genericType, 1);

        if (keyType == null || valueTypeData == null) return null;

        var valueTypeClass = valueTypeData.second();

        if (keyType != Identifier.class && keyType != String.class && !NumberReflection.isNumberType(keyType)) {
            return null;
        }

        JavaType valueJavaType;

        if (valueTypeClass == Identifier.class || valueTypeClass == String.class || NumberReflection.isNumberType(valueTypeClass)) {
            valueJavaType = JavaType.GENERIC;
        } else if (ReflectionUtils.getTypeArgument(valueTypeClass, 0) == null) {
            valueJavaType = JavaType.STRUCT;
        } else {
            return null;
        }

        return valueJavaType == JavaType.GENERIC ? CollectionType.SIMPLE : CollectionType.COMPLEX;
    }

    public enum CollectionType {
        SIMPLE, // Primitive-ish to Primitive-ish
        COMPLEX // Primitive-ish to Struct
    }

    @Nullable
    public static CollectionType getCollectionType(Type type) {
        var collectionType = ReflectionUtils.getTypeArgument(type, 0);

        if (collectionType == null) {
            return null;
        } else if (collectionType == Identifier.class || collectionType == String.class || NumberReflection.isNumberType(collectionType)) {
            return CollectionType.SIMPLE;
        } else if (ReflectionUtils.getTypeArgument(collectionType, 0) == null) {
            return CollectionType.COMPLEX;
        }

        return null;
    }

    public enum JavaType {
        GENERIC,
        STRUCT
    }

    public record RangeConstraintData(double min, double max, int decimalPlaces, boolean useSlider) {}
}
