package io.wispforest.owo.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.api.SyntaxError;

import java.io.File;
import java.io.IOException;

public class OwoConfig {

    public static void yes() {
        var jankson = new Jankson.Builder().build();

        try {
            final var load = jankson.load((new File("yes.json5")));

            System.out.println(load.getComment("yesValue"));
            System.out.println(jankson.getMarshaller().marshall(Yes.class, load).yesValue);

            load.setComment("yesValue", "no");
            System.out.println(load.toJson(true, true));
        } catch (IOException | SyntaxError e) {
            System.out.println("mald harder");
            e.printStackTrace();
        }

    }

    public static class Yes {
        int yesValue = 10;
    }

}
