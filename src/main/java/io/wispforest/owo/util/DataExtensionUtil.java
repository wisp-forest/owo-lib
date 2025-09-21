package io.wispforest.owo.util;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.SyntaxError;
import org.jetbrains.annotations.ApiStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@ApiStatus.Internal
public class DataExtensionUtil {
    public static final Jankson JANKSON = Jankson.builder().build();

    private DataExtensionUtil() {}

    public static InputStream coerceJson(InputStream inputStream) {
        try {
            return new CoercedByteArrayInputStream(JANKSON
                .load(inputStream)
                .toJson(JsonGrammar.STRICT)
                .getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException | SyntaxError e) {
            throw new RuntimeException("Failed to convert JSON5 to JSON", e);
        }
    }

    public static class CoercedByteArrayInputStream extends ByteArrayInputStream {
        public CoercedByteArrayInputStream(byte[] buf) {
            super(buf);
        }
    }
}
