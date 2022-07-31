package io.wispforest.uwu.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Config(name = "uwu", wrapperName = "UwuConfig")
public class UwuConfigModel {

    @RangeConstraint(min = 0, max = 56)
    public int aValue = 56;

    @RegexConstraint("[A-Za-z]{1,3}")
    public String regex = "yes";

    @Expanded
    public Nested nestingTime = new Nested();

    public float floting = 6.9f;

    public List<String> thereAreStringsHere = new ArrayList<>(List.of("yes", "no"));

    @Nest
    public static class Nested {
        public boolean togglee = false;
        public boolean yesThisIsAlsoNested = true;

        @Comment("Commented nesting")
        public SuperNested nestingTimeIntensifies = new SuperNested();

        public List<Integer> nestedIntegers = new ArrayList<>(List.of(69, 34, 35, 420));
    }

    @Nest
    public static class SuperNested {
        public byte wowSoNested;
    }

    @ExcludeFromScreen
    public String noSeeingThis = "yep, never";
}
