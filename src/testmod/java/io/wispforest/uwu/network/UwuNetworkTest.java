package io.wispforest.uwu.network;

import io.wispforest.owo.network.serialization.RecordSerializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.impl.RecordEndec;
import io.wispforest.owo.serialization.impl.ReflectionEndecBuilder;
import io.wispforest.owo.serialization.impl.StructEndecBuilder;
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

        var endec = RecordEndec.create(TestRecord.class);
        var sameendec = RecordEndec.create(TestRecord.class);

        testEquals(endec, sameendec);

        testSerialization(test, testRecord -> {
            return endec.decode(ByteBufDeserializer::new, endec.encode(ByteBufSerializer::packet, testRecord));
        });

        //--

        System.out.println();

        var builtendec = StructEndecBuilder.of(
                StructField.of("text", Endec.STRING.list().xmap(s -> s, s -> (List<String>) s), TestRecord::text),
                StructField.of("enumValue", ReflectionEndecBuilder.createEnumEndec(TestEnum.class), TestRecord::enumValue),
                TestRecord::new
        );

        testSerialization(test, testRecord -> {
            return builtendec.decode(ByteBufDeserializer::new, builtendec.encode(ByteBufSerializer::packet, testRecord));
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
