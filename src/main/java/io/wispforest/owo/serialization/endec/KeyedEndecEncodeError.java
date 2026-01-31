package io.wispforest.owo.serialization.endec;

import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.Owo;
import net.minecraft.util.ProblemReporter;

import java.io.PrintWriter;
import java.io.StringWriter;

public record KeyedEndecEncodeError(KeyedEndec<?> key, Object obj, Exception exception, boolean encodedDefaultValue, boolean sendEntireException) implements ProblemReporter.Problem {

    public KeyedEndecEncodeError(KeyedEndec<?> key, Object obj, Exception exception, boolean encodedDefaultValue) {
        this(key, obj, exception, encodedDefaultValue, Owo.DEBUG);
    }

    @Override
    public String description() {
        var message = new StringWriter();

        var writer = new PrintWriter(message);

        writer.println("Failed to encode value '" + this.obj + "' with KeyedEndec '" + this.key + "'" + (this.encodedDefaultValue ? "and used default value instead" : "") + ": ");

        if (sendEntireException) {
            writer.println(exception.getMessage());
        } else {
            exception.printStackTrace(writer);
        }

        return message.toString();
    }
}
