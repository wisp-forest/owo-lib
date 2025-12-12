package io.wispforest.owo.util;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.SyntaxError;
import com.google.common.collect.MapMaker;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Collections.newSetFromMap;

@ApiStatus.Internal
public class DataExtensionUtil {
    public static final ThreadLocal<Jankson> JANKSON = ThreadLocal.withInitial(() -> Jankson.builder().build());

    public static final Set<String> JSON5_ENABLED_PACKS = newSetFromMap(new MapMaker().weakKeys().makeMap());

    private DataExtensionUtil() {}

    public static InputStream coerceJson(InputStream inputStream) {
        try {
            return new CoercedByteArrayInputStream(JANKSON
                .get()
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

    public interface OptInIdentifierPredicate extends Predicate<Identifier> {
        static OptInIdentifierPredicate of(Predicate<Identifier> delegate) {
            return delegate instanceof OptInIdentifierPredicate optIn ? optIn : delegate::test;
        }
    }
}
