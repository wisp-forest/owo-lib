package io.wispforest.uwu.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Modmenu(modId = "uwu", uiModelId = "uwu:config")
@Config(name = "uwu", wrapperName = "UwuConfig")
public class UwuConfigModel {

    @SectionHeader("top")
    @RangeConstraint(min = 0, max = 56)
    public int aValue = 56;

    @RegexConstraint("[A-Za-z]{1,3}")
    public String regex = "yes";

    @Nest
    @Expanded
    public Nested nestingTime = new Nested();

    public float floting = 6.9f;

    @SectionHeader("bottom")
    public List<String> thereAreStringsHere = new ArrayList<>(List.of("yes", "no"));

    @RestartRequired
    public WowValues broTheresAnEnum = WowValues.FIRST;

    @ExcludeFromScreen
    public String noSeeingThis = "yep, never";

    public static class Nested {
        public boolean togglee = false;
        public boolean yesThisIsAlsoNested = true;

        @Nest
        @Comment("Commented nesting")
        public SuperNested nestingTimeIntensifies = new SuperNested();

        @Sync(Option.SyncMode.INFORM_SERVER)
        public List<Integer> nestedIntegers = new ArrayList<>(List.of(69, 34, 35, 420));
    }

    public static class SuperNested {
        public byte wowSoNested;
    }

    public enum WowValues {
        FIRST, SECOND, THIRD, FOURTH;
    }
}
