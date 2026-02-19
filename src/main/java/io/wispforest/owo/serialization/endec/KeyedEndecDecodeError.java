package io.wispforest.owo.serialization.endec;

import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.Owo;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;

import java.io.PrintWriter;
import java.io.StringWriter;

// TODO: GET ENDEC TRACE WHEN USING LATEST ENDEC OR SOMETHING?
public record KeyedEndecDecodeError(KeyedEndec<?> key, Tag element, Exception exception, boolean sendEntireException) implements ProblemReporter.Problem {

    public KeyedEndecDecodeError(KeyedEndec<?> key, Tag element, Exception exception) {
        this(key, element, exception, Owo.DEBUG);
    }

    @Override
    public String description() {
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
