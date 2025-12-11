package io.wispforest.owo.braid.widgets.label;

import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.instance.TooltipProvider;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.mixin.braid.ClickHandlerAccessor;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
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
    public final Text text;

    public RawLabel(LabelStyle style, boolean softWrap, boolean ellipsize, Text text) {
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

        private List<OrderedText> renderText = List.of();
        private DoubleList renderTextWidths = new DoubleArrayList();
        private int renderTextHeight = 0;

        protected Function<Style, Boolean> textClickHandler = style -> {
            return style != null && OwoUIDrawContext.utilityScreen().handleTextClick(style, MinecraftClient.getInstance().currentScreen);
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

        protected List<OrderedText> wrapText(TextRenderer textRenderer, int maxWidth, double maxHeight) {
            var styledText = this.widget.text.copy().styled(textStyle -> textStyle.withParent(this.widget.style.textStyle()));
            var wrappedLines = textRenderer.getTextHandler().wrapLines(styledText, this.widget.softWrap ? maxWidth : Integer.MAX_VALUE, Style.EMPTY);

            var maxLines = (int) Math.floor(maxHeight / textRenderer.fontHeight);
            if (this.widget.ellipsize && !wrappedLines.isEmpty() && maxLines > 0 && (wrappedLines.size() > maxLines || textRenderer.getWidth(wrappedLines.getLast()) > maxWidth)) {
                wrappedLines = wrappedLines.subList(0, maxLines);

                var ellipsis = StringVisitable.plain("…");
                var ellipsisLength = textRenderer.getWidth(ellipsis);

                var trimmedLastLine = textRenderer.trimToWidth(wrappedLines.getLast(), maxWidth - ellipsisLength);
                wrappedLines.set(
                    wrappedLines.size() - 1,
                    StringVisitable.concat(trimmedLastLine, ellipsis)
                );
            }

            return Language.getInstance().reorder(wrappedLines);
        }

        protected TextMetrics measureText(TextRenderer textRenderer, List<OrderedText> lines) {
            var textWidth = 0;
            var textHeight = 0;
            var lineWidths = new DoubleArrayList();

            for (var line : lines) {
                var lineWidth = textRenderer.getWidth(line);
                lineWidths.add(lineWidth);

                textWidth = Math.max(textWidth, lineWidth);
                textHeight += textRenderer.fontHeight;
            }

            return new TextMetrics(textWidth, textHeight, lineWidths);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var textRenderer = this.host().client().textRenderer;
            this.renderText = this.wrapText(textRenderer, (int) constraints.maxWidth(), (int) constraints.maxHeight());

            var metrics = this.measureText(textRenderer, this.renderText);

            this.renderTextWidths = metrics.lineWidths();
            this.renderTextHeight = metrics.height();

            var size = Size.of(metrics.width, metrics.height).constrained(constraints);
            this.transform.setSize(size);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            var renderer = this.host().client().textRenderer;
            return this.measureText(renderer, this.wrapText(renderer, Integer.MAX_VALUE, (int) height)).width;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            var renderer = this.host().client().textRenderer;
            return this.measureText(renderer, this.wrapText(renderer, this.widget.softWrap ? (int) width : Integer.MAX_VALUE, Integer.MAX_VALUE)).height;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.of(this.host().client().textRenderer.fontHeight - 2);
        }

        @Override
        public void draw(BraidDrawContext ctx) {
            var textRenderer = this.host().client().textRenderer;
            var yOffset = this.widget.style.textAlignment().alignVertical(this.transform.height(), this.renderTextHeight);

            for (int lineIdx = 0; lineIdx < this.renderText.size(); lineIdx++) {
                ctx.drawText(
                    textRenderer,
                    this.renderText.get(lineIdx),
                    (int) this.widget.style.textAlignment().alignHorizontal(this.transform.width(), this.renderTextWidths.getDouble(lineIdx)),
                    (int) yOffset + lineIdx * textRenderer.fontHeight,
                    this.widget.style.baseColor().argb(),
                    this.widget.style.shadow()
                );
            }
        }

        // this reimplementation of RawLabel.draw is pretty cringe, however
        // mojang has left our hands tied since the text collector interface
        // does not give us control over text color and shadow
        public void collectText(DrawnTextConsumer collector) {
            var textRenderer = this.host().client().textRenderer;
            var yOffset = this.widget.style.textAlignment().alignVertical(this.transform.height(), this.renderTextHeight);

            for (int lineIdx = 0; lineIdx < this.renderText.size(); lineIdx++) {
                collector.text(
                    (int) this.widget.style.textAlignment().alignHorizontal(this.transform.width(), this.renderTextWidths.getDouble(lineIdx)),
                    (int) yOffset + lineIdx * textRenderer.fontHeight,
                    this.renderText.get(lineIdx)
                );
            }
        }

        @Override
        @Nullable
        public List<TooltipComponent> getTooltipComponentsAt(double x, double y) {
            return null;
        }

        @Override
        @Nullable
        public Style getStyleAt(double x, double y) {
            if (this.renderText.isEmpty()) return null;

            var transform = this.computeGlobalTransform().invert();
            var clickPos = transform.transformPosition((float) x, (float) y, new Vector2f());

            var collector = new StyleCollector(this.host().client().textRenderer, (int) clickPos.x, (int) clickPos.y);
            this.collectText(collector);

            return collector.getStyle();
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

        public static class StyleCollector extends DrawnTextConsumer.ClickHandler {

            public StyleCollector(TextRenderer textRenderer, int clickX, int clickY) {
                super(textRenderer, clickX, clickY);
                ((ClickHandlerAccessor) this).owo$setSetStyleCallback(((ClickHandlerAccessor) this)::owo$setStyle);
            }
        }
    }

    public record TextMetrics(int width, int height, DoubleList lineWidths) {}
}
