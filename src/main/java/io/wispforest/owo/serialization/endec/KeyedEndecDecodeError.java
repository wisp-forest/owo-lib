package io.wispforest.owo.serialization.endec;

import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.Owo;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.ErrorReporter;

import java.io.PrintWriter;
import java.io.StringWriter;

// TODO: GET ENDEC TRACE WHEN USING LATEST ENDEC OR SOMETHING?
public record KeyedEndecDecodeError(KeyedEndec<?> key, NbtElement element, Exception exception, boolean sendEntireException) implements ErrorReporter.Error {

    public KeyedEndecDecodeError(KeyedEndec<?> key, NbtElement element, Exception exception) {
        this(key, element, exception, Owo.DEBUG);
    }

    @Override
    public String getMessage() {
        var message = new StringWriter();

        var writer = new PrintWriter(message);

        writer.println("Failed to decode value '" + this.element + "' from KeyedEndec '" + this.key.key() + "': ");

        if (sendEntireException) {
            writer.println(exception.getMessage());
        } else {
            exception.printStackTrace(writer);
        }

        return message.toString();
    }
}
