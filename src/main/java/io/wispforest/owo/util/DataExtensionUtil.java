package io.wispforest.owo.util;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.SyntaxError;
import io.wispforest.owo.Owo;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

@ApiStatus.Internal
public class DataExtensionUtil {
    public static final Jankson JANKSON = Jankson.builder().build();

    private DataExtensionUtil() {}

    //region Coercion

    public static InputStream coerceJson(InputStream inputStream) {
        try {
            return new CoercedByteArrayInputStream(JANKSON
                .load(inputStream)
                .toJson(JsonGrammar.STRICT)
                .getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException | SyntaxError e) {
            throw new RuntimeException("Failed to coerce JSON", e);
        }
    }

    public static class CoercedByteArrayInputStream extends ByteArrayInputStream {
        public CoercedByteArrayInputStream(byte[] buf) {
            super(buf);
        }
    }

    //endregion
}
