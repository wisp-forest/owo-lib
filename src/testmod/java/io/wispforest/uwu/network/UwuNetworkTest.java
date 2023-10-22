package io.wispforest.uwu.network;

import io.wispforest.owo.network.serialization.RecordSerializer;
import io.wispforest.owo.serialization.Codeck;
import io.wispforest.owo.serialization.impl.RecordCodeck;
import io.wispforest.owo.serialization.impl.ReflectionCodeckBuilder;
import io.wispforest.owo.serialization.impl.StructCodeckBuilder;
import io.wispforest.owo.serialization.impl.StructField;
import io.wispforest.owo.serialization.impl.bytebuf.ByteBufDeserializer;
import io.wispforest.owo.serialization.impl.bytebuf.ByteBufSerializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class UwuNetworkTest {

    public static void main(String[] args) {
        var test = new TestRecord(new LinkedList<>(List.of("hahayes epic text")), TestEnum.ANOTHER_VALUE);
        var serializer = RecordSerializer.create(TestRecord.class);
        var sameSerializer = RecordSerializer.create(TestRecord.class);

        testEquals(serializer, sameSerializer);

        testSerialization(test, testRecord -> {
            var buffer = PacketByteBufs.create();
            return serializer.write(buffer, test).read(buffer);
        });

        //--

        System.out.println();

        var codeck = RecordCodeck.create(TestRecord.class);
        var sameCodeck = RecordCodeck.create(TestRecord.class);

        testEquals(codeck, sameCodeck);

        testSerialization(test, testRecord -> {
            return codeck.decode(ByteBufDeserializer::new, codeck.encode(ByteBufSerializer::packet, testRecord));
        });

        //--

        System.out.println();

        var builtCodeck = StructCodeckBuilder.of(
                StructField.of("text", Codeck.STRING.list().then(s -> s, s -> (List<String>) s), TestRecord::text),
                StructField.of("enumValue", ReflectionCodeckBuilder.createEnumSerializer(TestEnum.class), TestRecord::enumValue),
                TestRecord::new
        );

        testSerialization(test, testRecord -> {
            return builtCodeck.decode(ByteBufDeserializer::new, builtCodeck.encode(ByteBufSerializer::packet, testRecord));
        });
    }

    public static void testSerialization(TestRecord test, Function<TestRecord, TestRecord> function){
        var read = function.apply(test);

        testEquals(test, read);
    }

    public record TestRecord(Collection<String> text, TestEnum enumValue) {}

    public enum TestEnum {ONE_VALUE, ANOTHER_VALUE}

    private static <T> void testEquals(T object, T other) {
        testEquals(object, other, Objects::toString, Object::equals);
    }

    private static <T> void testEquals(T object, T other, Function<T, String> formatter, BiPredicate<T, T> predicate) {
        System.out.println("Comparing '" + formatter.apply(object) + "' to '" + formatter.apply(other) + "'");
        System.out.println("object == other -> " + (object == other));
        System.out.println("predicate.test(object, other) -> " + predicate.test(object, other));
    }

}
