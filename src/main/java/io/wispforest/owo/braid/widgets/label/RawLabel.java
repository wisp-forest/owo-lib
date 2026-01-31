package io.wispforest.owo.braid.widgets.label;

import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.instance.TooltipProvider;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.mixin.braid.ClickableStyleFinderAccessor;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.Function;

public class RawLabel extends LeafInstanceWidget {

    public final LabelStyle style;
    public final boolean softWrap;
    public final boolean ellipsize;
    public final Component text;

    public RawLabel(LabelStyle style, boolean softWrap, boolean ellipsize, Component text) {
        this.style = style;
        this.softWrap = softWrap;
        this.ellipsize = ellipsize;
        this.text = text;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<RawLabel> implements TooltipProvider, MouseListener {

        private List<FormattedCharSequence> renderText = List.of();
        private DoubleList renderTextWidths = new DoubleArrayList();
        private int renderTextHeight = 0;

        protected Function<Style, Boolean> textClickHandler = style -> {
            return style != null && OwoUIGraphics.utilityScreen().handleTextClick(style, Minecraft.getInstance().screen);
        };

        public Instance(RawLabel widget) {
            super(widget);
        }

        @Override
        public void setWidget(RawLabel widget) {
            if (Objects.equals(this.widget.style, widget.style)
                && this.widget.softWrap == widget.softWrap
                && this.widget.ellipsize == widget.ellipsize
                && Objects.equals(this.widget.text, widget.text)) {
                return;
            }

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        protected List<FormattedCharSequence> wrapText(Font font, int maxWidth, double maxHeight) {
            var styledText = this.widget.text.copy().withStyle(textStyle -> textStyle.applyTo(this.widget.style.textStyle()));
            var wrappedLines = font.getSplitter().splitLines(styledText, this.widget.softWrap ? maxWidth : Integer.MAX_VALUE, Style.EMPTY);

            var maxLines = (int) Math.floor(maxHeight / font.lineHeight);
            if (this.widget.ellipsize && !wrappedLines.isEmpty() && maxLines > 0 && (wrappedLines.size() > maxLines || font.width(wrappedLines.getLast()) > maxWidth)) {
                wrappedLines = wrappedLines.subList(0, maxLines);

                var ellipsis = FormattedText.of("…");
                var ellipsisLength = font.width(ellipsis);

                var trimmedLastLine = font.substrByWidth(wrappedLines.getLast(), maxWidth - ellipsisLength);
                wrappedLines.set(
                    wrappedLines.size() - 1,
                    FormattedText.composite(trimmedLastLine, ellipsis)
                );
            }

            return Language.getInstance().getVisualOrder(wrappedLines);
        }

        protected TextMetrics measureText(Font font, List<FormattedCharSequence> lines) {
            var textWidth = 0;
            var textHeight = 0;
            var lineWidths = new DoubleArrayList();

            for (var line : lines) {
                var lineWidth = font.width(line);
                lineWidths.add(lineWidth);

                textWidth = Math.max(textWidth, lineWidth);
                textHeight += font.lineHeight;
            }

            return new TextMetrics(textWidth, textHeight, lineWidths);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var font = this.host().client().font;
            this.renderText = this.wrapText(font, (int) constraints.maxWidth(), (int) constraints.maxHeight());

            var metrics = this.measureText(font, this.renderText);

            this.renderTextWidths = metrics.lineWidths();
            this.renderTextHeight = metrics.height();

            var size = Size.of(metrics.width, metrics.height).constrained(constraints);
            this.transform.setSize(size);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            var renderer = this.host().client().font;
            return this.measureText(renderer, this.wrapText(renderer, Integer.MAX_VALUE, (int) height)).width;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            var renderer = this.host().client().font;
            return this.measureText(renderer, this.wrapText(renderer, this.widget.softWrap ? (int) width : Integer.MAX_VALUE, Integer.MAX_VALUE)).height;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.of(this.host().client().font.lineHeight - 2);
        }

        @Override
        public void draw(BraidGraphics graphics) {
            var font = this.host().client().font;
            var yOffset = this.widget.style.textAlignment().alignVertical(this.transform.height(), this.renderTextHeight);

            for (int lineIdx = 0; lineIdx < this.renderText.size(); lineIdx++) {
                graphics.drawString(
                    font,
                    this.renderText.get(lineIdx),
                    (int) this.widget.style.textAlignment().alignHorizontal(this.transform.width(), this.renderTextWidths.getDouble(lineIdx)),
                    (int) yOffset + lineIdx * font.lineHeight,
                    this.widget.style.baseColor().argb(),
                    this.widget.style.shadow()
                );
            }
        }

        // this reimplementation of RawLabel.draw is pretty cringe, however
        // mojang has left our hands tied since the text collector interface
        // does not give us control over text color and shadow
        public void collectText(ActiveTextCollector collector) {
            var font = this.host().client().font;
            var yOffset = this.widget.style.textAlignment().alignVertical(this.transform.height(), this.renderTextHeight);

            for (int lineIdx = 0; lineIdx < this.renderText.size(); lineIdx++) {
                collector.accept(
                    (int) this.widget.style.textAlignment().alignHorizontal(this.transform.width(), this.renderTextWidths.getDouble(lineIdx)),
                    (int) yOffset + lineIdx * font.lineHeight,
                    this.renderText.get(lineIdx)
                );
            }
        }

        @Override
        @Nullable
        public List<ClientTooltipComponent> getTooltipComponentsAt(double x, double y) {
            return null;
        }

        @Override
        @Nullable
        public Style getStyleAt(double x, double y) {
            if (this.renderText.isEmpty()) return null;

            var collector = new StyleCollector(this.host().client().font, (int) x, (int) y);
            this.collectText(collector);

            return collector.result();
        }

        @Override
        public boolean onMouseDown(double x, double y, int button, KeyModifiers modifiers) {
            if (button != 0) return MouseListener.super.onMouseDown(x, y, button, modifiers);
            return this.textClickHandler.apply(this.getStyleAt(x, y));
        }

        @Override
        public @Nullable CursorStyle cursorStyleAt(double x, double y) {
            var style = this.getStyleAt(x, y);
            if (style == null) return null;
            if (style.getClickEvent() != null) return CursorStyle.HAND;
            return null;
        }

        public static class StyleCollector extends ActiveTextCollector.ClickableStyleFinder {

            public StyleCollector(Font font, int clickX, int clickY) {
                super(font, clickX, clickY);
                ((ClickableStyleFinderAccessor) this).owo$setStyleScanner(((ClickableStyleFinderAccessor) this)::owo$setResult);
            }
        }
    }

    public record TextMetrics(int width, int height, DoubleList lineWidths) {}
}
