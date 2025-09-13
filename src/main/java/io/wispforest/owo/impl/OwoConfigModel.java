package io.wispforest.owo.impl;

import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.util.Wisdom;

@Modmenu(modId = "owo")
@Config(wrapperName = "OwoConfigImpl", name = "owo")
public class OwoConfigModel {

    @SectionHeader("condensed_entries")
    public boolean expandedCondensedEntries = false;

    public boolean showBackgroundColor = true;
    public boolean showBorderColor = true;

    @WithAlpha
    public Color borderColor = Color.ofArgb(0xFF3955e5);
}
