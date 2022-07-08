package io.wispforest.uwu.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.RegexConstraint;

@Config(name = "uwu", wrapperName = "UwuConfig")
public class UwuConfigModel {

    @RangeConstraint(min = 0, max = 56)
    public int aValue = 56;

    @RegexConstraint("[A-Za-z]{1,3}")
    public String regexe = "yes";

    public float floting = 6.9f;
}
