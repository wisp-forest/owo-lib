package io.wispforest.uwu.network;

import io.wispforest.endec.Endec;
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import io.wispforest.endec.impl.RecordEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class UwuNetworkTest {

    public static void main(String[] args) {
        var test = new TestRecord(new LinkedList<>(List.of("hahayes epic text")), TestEnum.ANOTHER_VALUE);
        var serializer = RecordEndec.create(TestRecord.class);
        var sameSerializer = RecordEndec.create(TestRecord.class);

        testEquals(serializer, sameSerializer);

        testSerialization(test, testRecord -> {
            var buffer = PacketByteBufs.create();
            buffer.write(serializer, test);
            return buffer.read(serializer);
        });

        //--

        System.out.println();

        var endec = RecordEndec.create(TestRecord.class);
        var sameendec = RecordEndec.create(TestRecord.class);

        testEquals(endec, sameendec);

        testSerialization(test, testRecord -> {
            return endec.decodeFully(ByteBufDeserializer::of, endec.encodeFully(() -> ByteBufSerializer.of(PacketByteBufs.create()), testRecord));
        });

        //--

        System.out.println();

        var builtendec = StructEndecBuilder.of(
                Endec.STRING.listOf().xmap(s -> s, s -> s).fieldOf("text", TestRecord::text),
                Endec.forEnum(TestEnum.class).fieldOf("enumValue", TestRecord::enumValue),
                TestRecord::new
        );

        testSerialization(test, testRecord -> {
            return builtendec.decodeFully(ByteBufDeserializer::of, builtendec.encodeFully(() -> ByteBufSerializer.of(PacketByteBufs.create()), testRecord));
        });
    }

    public static void testSerialization(TestRecord test, Function<TestRecord, TestRecord> function){
        var read = function.apply(test);

        testEquals(test, read);
    }

    public record TestRecord(List<String> text, TestEnum enumValue) {}

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
