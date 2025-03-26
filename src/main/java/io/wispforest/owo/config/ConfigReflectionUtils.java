package io.wispforest.owo.config;

import io.wispforest.owo.config.annotation.PredicateConstraint;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.RegexConstraint;
import io.wispforest.owo.config.base.BoundedAccess;
import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.options.ReflectiveOption;
import io.wispforest.owo.util.NumberReflection;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ConfigReflectionUtils {

    public static RangeConstraintData getConstraintData(Class<? extends Number> clazz, OptionControlSpec<?> option) {
        @Nullable BoundedAccess<?> possibleAccess = option instanceof ReflectiveOption<?> reflectiveOption
                ? reflectiveOption.backingAccess()
                : null;

        return getConstraintData(clazz, possibleAccess);
    }

    public static RangeConstraintData getConstraintData(Class<? extends Number> clazz, @Nullable BoundedAccess<?> access) {
        var floatingPointType = NumberReflection.isFloatingPointType(clazz);

        var useSlider = false;

        var decimalPlaces = floatingPointType ? 2 : 0;

        double min = NumberReflection.minValue(clazz).doubleValue(), max = NumberReflection.maxValue(clazz).doubleValue();

        if (access != null && access.hasAnnotation(RangeConstraint.class)) {
            var constraintData = access.getAnnotation(RangeConstraint.class);

            useSlider = constraintData.useSlider();

            if (floatingPointType) decimalPlaces = constraintData.decimalPlaces();

            min = constraintData.min();
            max = constraintData.max();
        }

        return new RangeConstraintData(min, max, decimalPlaces, useSlider);
    }

    @Nullable
    public static <T> ConfigWrapper.Constraint getConstraint(BoundedAccess<T> boundField) throws IllegalAccessException, NoSuchMethodException {
        var fieldType = boundField.type();

        ConfigWrapper.Constraint constraint = null;

        if (boundField.hasAnnotation(RangeConstraint.class)) {
            var annotation = boundField.getAnnotation(RangeConstraint.class);

            if (NumberReflection.isNumberType(fieldType)) {
                Predicate<?> predicate;
                if (fieldType == long.class || fieldType == Long.class) {
                    predicate = o -> o != null && (Long) o >= annotation.min() && (Long) o <= annotation.max();
                } else {
                    predicate = o -> o != null && ((Number) o).doubleValue() >= annotation.min() && ((Number) o).doubleValue() <= annotation.max();
                }

                constraint = new ConfigWrapper.Constraint("Range from " + annotation.min() + " to " + annotation.max(), predicate);
            } else {
                throw new IllegalStateException("@RangeConstraint can only be applied to numeric fields");
            }
        }

        if (boundField.hasAnnotation(RegexConstraint.class)) {
            var annotation = boundField.getAnnotation(RegexConstraint.class);

            if (CharSequence.class.isAssignableFrom(fieldType)) {
                var pattern = Pattern.compile(annotation.value());
                constraint = new ConfigWrapper.Constraint("Regex " + annotation.value(), o -> o != null && pattern.matcher((CharSequence) o).matches());
            } else {
                throw new IllegalStateException("@RegexConstraint can only be applied to fields with a string representation");
            }
        }

        if (boundField.hasAnnotation(PredicateConstraint.class)) {
            var annotation = boundField.getAnnotation(PredicateConstraint.class);
            var method = boundField.owner().getClass().getMethod(annotation.value(), fieldType);

            if (method.getReturnType() != boolean.class) {
                throw new NoSuchMethodException("Return type of predicate implementation '" + annotation.value() + "' must be 'boolean'");
            }

            if (!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalStateException("Predicate implementation '" + annotation.value() + "' must be static");
            }

            var handle = MethodHandles.publicLookup().unreflect(method);
            constraint = new ConfigWrapper.Constraint("Predicate method " + annotation.value(), o -> invokePredicate(handle, o));
        }

        return constraint;
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
        if (collectionType == null) return null;

        if (collectionType == Identifier.class || collectionType == String.class || NumberReflection.isNumberType(collectionType)) {
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
