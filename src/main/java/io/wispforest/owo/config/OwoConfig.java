package io.wispforest.owo.config;

import java.lang.reflect.Field;

public class OwoConfig {

    public static <T> T instantiate(Class<T> clazz) {
        return null;
    }

    public static Field getField(Class<?> configClass, String key) {
        var pathElements = key.split("\\.");

        Class<?> clazz = configClass;
        Field field = null;

        for (var element : pathElements) {
            try {
                field = clazz.getDeclaredField(element);
                clazz = field.getType();
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        return field;
    }

}
