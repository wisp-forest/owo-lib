package io.wispforest.owo.util;

import org.jetbrains.annotations.ApiStatus;

/**
 * Slightly concerning and experimental helpers for working
 * with the reflective {@link Class} objects of the
 * number primitives and their wrappers
 */
@ApiStatus.Experimental
public final class NumberReflection {

    private NumberReflection() {}

    /**
     * Determines whether the given class represents a number type
     *
     * @param clazz The class to test
     * @return {@code true} if {@code clazz} is either a primitive
     * number type or one the respective wrappers
     */
    public static boolean isNumberType(Class<?> clazz) {
        return (clazz.isPrimitive() && clazz != boolean.class && clazz != char.class)
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Double.class
                || clazz == Float.class;
    }

    /**
     * Determines whether the given class represents
     * a floating point type
     *
     * @param clazz The class to test
     * @return {@code true} if {@code clazz} is either a primitive floating point type
     * or {@link Float} or {@link Double}
     */
    public static boolean isFloatingPointType(Class<?> clazz) {
        return clazz == Float.class || clazz == float.class || clazz == Double.class || clazz == double.class;
    }

    /**
     * Tries to convert the given number to {@code targetClass}
     * by calling the corresponding {@code Number.<type>Value()} method
     *
     * @param in          The number to convert
     * @param targetClass The target class, must be something which satisfies {@link #isNumberType(Class)}
     * @return The input number, converted to the target type
     * @throws IllegalArgumentException if either {@code targetClass} does not satisfy {@link #isNumberType(Class)}
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> T convert(Number in, Class<T> targetClass) {
        if (!isNumberType(targetClass)) throw new IllegalArgumentException("Cannot convert to non-number target class");

        if (targetClass == Float.class || targetClass == float.class) {
            return (T) (Float) in.floatValue();
        } else if (targetClass == Double.class || targetClass == double.class) {
            return (T) (Double) in.doubleValue();
        } else if (targetClass == Byte.class || targetClass == byte.class) {
            return (T) (Byte) in.byteValue();
        } else if (targetClass == Short.class || targetClass == short.class) {
            return (T) (Short) in.shortValue();
        } else if (targetClass == Integer.class || targetClass == int.class) {
            return (T) (Integer) in.intValue();
        } else if (targetClass == Long.class || targetClass == long.class) {
            return (T) (Long) in.longValue();
        } else {
            throw new IllegalStateException("Target class does not correspond to a supported number type - this should be unreachable");
        }
    }

    /**
     * Tries to determine the maximum value supported by the number
     * type which {@code numberType} represents
     *
     * @param numberType The target number type, must be something which satisfies {@link #isNumberType(Class)}
     * @return The maximum value of the given number type
     * @throws IllegalArgumentException if either {@code targetClass} does not satisfy {@link #isNumberType(Class)}
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> T maxValue(Class<T> numberType) {
        if (!isNumberType(numberType)) throw new IllegalArgumentException("Cannot get maximum value of non-number class");

        if (numberType == Float.class || numberType == float.class) {
            return (T) (Float) Float.MAX_VALUE;
        } else if (numberType == Double.class || numberType == double.class) {
            return (T) (Double) Double.MAX_VALUE;
        } else if (numberType == Byte.class || numberType == byte.class) {
            return (T) (Byte) Byte.MAX_VALUE;
        } else if (numberType == Short.class || numberType == short.class) {
            return (T) (Short) Short.MAX_VALUE;
        } else if (numberType == Integer.class || numberType == int.class) {
            return (T) (Integer) Integer.MAX_VALUE;
        } else if (numberType == Long.class || numberType == long.class) {
            return (T) (Long) Long.MAX_VALUE;
        } else {
            throw new IllegalStateException("Target class does not correspond to a supported number type - this should be unreachable");
        }
    }

    /**
     * Tries to determine the minimum value supported by the number
     * type which {@code numberType} represents
     *
     * @param numberType The target number type, must be something which satisfies {@link #isNumberType(Class)}
     * @return The minimum value of the given number type
     * @throws IllegalArgumentException if either {@code targetClass} does not satisfy {@link #isNumberType(Class)}
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> T minValue(Class<T> numberType) {
        if (!isNumberType(numberType)) throw new IllegalArgumentException("Cannot get minimum value of non-number class");

        if (numberType == Float.class || numberType == float.class) {
            return (T) (Float) (-Float.MAX_VALUE);
        } else if (numberType == Double.class || numberType == double.class) {
            return (T) (Double) (-Double.MAX_VALUE);
        } else if (numberType == Byte.class || numberType == byte.class) {
            return (T) (Byte) Byte.MIN_VALUE;
        } else if (numberType == Short.class || numberType == short.class) {
            return (T) (Short) Short.MIN_VALUE;
        } else if (numberType == Integer.class || numberType == int.class) {
            return (T) (Integer) Integer.MIN_VALUE;
        } else if (numberType == Long.class || numberType == long.class) {
            return (T) (Long) Long.MIN_VALUE;
        } else {
            throw new IllegalStateException("Target class does not correspond to a supported number type - this should be unreachable");
        }
    }
}
