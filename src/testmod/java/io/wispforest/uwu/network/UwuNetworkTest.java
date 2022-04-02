
package io.wispforest.uwu.network;

import io.wispforest.owo.network.annotations.ElementType;
import io.wispforest.owo.network.serialization.RecordSerializer;
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

        var buffer = PacketByteBufs.create();
        var read = serializer.write(buffer, test).read(buffer);

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
