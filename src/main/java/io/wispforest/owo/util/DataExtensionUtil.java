package io.wispforest.owo.util;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.SyntaxError;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DataExtensionUtil {
    public static final Jankson JANKSON = Jankson.builder().build();

    public static InputStream coerceJson(InputStream inputStream) {
        try {
            return new ByteArrayInputStream(
                JANKSON
                    .load(inputStream)
                    .toJson(JsonGrammar.STRICT)
                    .getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException | SyntaxError e) {
            throw new RuntimeException("Failed to parse JSON5 input stream", e);
        }
    }
}
