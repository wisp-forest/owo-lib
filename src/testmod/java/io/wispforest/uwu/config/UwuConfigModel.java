package io.wispforest.uwu.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.RegexConstraint;

import java.util.ArrayList;
import java.util.List;

@Config(name = "uwu", wrapperName = "UwuConfig")
public class UwuConfigModel {

    @RangeConstraint(min = 0, max = 56)
    public int aValue = 56;

    @RegexConstraint("[A-Za-z]{1,3}")
    public String regex = "yes";

    public Nested nestingTime = new Nested();

    public float floting = 6.9f;

    public List<String> thereAreStringsHere = new ArrayList<>(List.of("yes", "no"));

    @Nest
    public static class Nested {
        public boolean togglee = false;
        public boolean yesThisIsAlsoNested = true;

        @Comment("Commented nesting")
        public SuperNested nestingTimeIntensifies = new SuperNested();
    }

    @Nest
    public static class SuperNested {
        public byte wowSoNested;
    }
}
