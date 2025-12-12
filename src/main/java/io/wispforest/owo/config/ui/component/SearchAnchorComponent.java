package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SearchAnchorComponent extends BaseUIComponent {

    protected final ParentUIComponent anchorFrame;
    protected final Supplier<String>[] searchTextSources;
    protected final Option.Key key;

    protected Consumer<ConfigScreen.SearchHighlighterComponent> highlightConfigurator = highlight -> {};

    @SafeVarargs
    public SearchAnchorComponent(ParentUIComponent anchorFrame, Option.Key key, Supplier<String>... searchTextSources) {
        this.anchorFrame = anchorFrame;
        this.searchTextSources = searchTextSources;
        this.key = key;

        this.positioning(Positioning.absolute(0, 0));
        this.sizing(Sizing.fixed(0));
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {}

    public ParentUIComponent anchorFrame() {
        return this.anchorFrame;
    }

    public ConfigScreen.SearchHighlighterComponent configure(ConfigScreen.SearchHighlighterComponent component) {
        this.highlightConfigurator.accept(component);
        return component;
    }

    public SearchAnchorComponent highlightConfigurator(Consumer<ConfigScreen.SearchHighlighterComponent> highlightConfigurator) {
        this.highlightConfigurator = highlightConfigurator;
        return this;
    }

    public Option.Key key() {
        return this.key;
    }

    public String currentSearchText() {
        return Arrays.stream(this.searchTextSources)
                .map(Supplier::get)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.joining());
    }
}
