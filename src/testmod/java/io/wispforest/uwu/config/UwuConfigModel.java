package io.wispforest.uwu.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.PredicateConstraint;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.RegexConstraint;

@Config(name = "uwu", wrapperName = "UwuConfig")
public class UwuConfigModel {

    @RangeConstraint(min = 0, max = 56)
    public int aValue = 56;

    @RegexConstraint("[A-Za-z]{3}")
    public String regexe = "yes";

    @PredicateConstraint("testFloting")
    public float floting = 6.9f;

    public static boolean testFloting(float floting) {
        return floting == 6.9f;
    }
}
