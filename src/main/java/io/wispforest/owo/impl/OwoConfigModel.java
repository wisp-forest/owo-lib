package io.wispforest.owo.impl;

import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.ui.core.Color;

@Modmenu(modId = "owo")
@Config(wrapperName = "OwoConfigImpl", name = "owo")
public class OwoConfigModel {

    @SectionHeader("condensed_entries")
    public boolean expandedCondensedEntries = false;

    public boolean showBackgroundColor = true;
    public boolean showBorderColor = true;

    @WithAlpha
    public Color borderColor = Color.ofArgb(0xFF3955e5);

    public boolean showEntryShuffle = false;

    @RangeConstraint(min = 0.5f, max = Float.MAX_VALUE)
    public float entryShuffleTime = 1.5f;

    public boolean showExpandEntriesButton = true;

    @Nest
    public AdvancedTooltipInfo info = new AdvancedTooltipInfo();

    public static class AdvancedTooltipInfo {
        public boolean showTagData = true;
        public boolean showEntryData = true;
    }
}
