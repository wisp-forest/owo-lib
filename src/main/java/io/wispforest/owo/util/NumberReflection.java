package io.wispforest.owo.util;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class NumberReflection {

    public static boolean isNumberType(Class<?> clazz) {
        return (clazz.isPrimitive() && clazz != boolean.class  && clazz != char.class) || Number.class.isAssignableFrom(clazz);
    }

    public static boolean isFloatingPointType(Class<?> clazz) {
        return clazz == Float.class || clazz == float.class || clazz == Double.class || clazz == double.class;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> T convert(Number in, Class<T> targetClass) {
        if (!isNumberType(targetClass)) throw new IllegalStateException("Cannot convert to non-number target class");

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
            throw new IllegalStateException("Target class does not correspond to a supported number type");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> T maxValue(Class<T> numberType) {
        if (!isNumberType(numberType)) throw new IllegalStateException("Cannot get maximum value of non-number class");

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
            throw new IllegalStateException("Target class does not correspond to a supported number type");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> T minValue(Class<T> numberType) {
        if (!isNumberType(numberType)) throw new IllegalStateException("Cannot get minimum value of non-number class");

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
            throw new IllegalStateException("Target class does not correspond to a supported number type");
        }
    }
}
