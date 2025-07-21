package io.wispforest.owo.braid.widgets.label;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

public class RawLabel extends LeafInstanceWidget {

    public final LabelStyle style;
    public final boolean softWrap;
    public final Text text;

    public RawLabel(LabelStyle style, boolean softWrap, Text text) {
        this.style = style;
        this.softWrap = softWrap;
        this.text = text;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<RawLabel> {

        private List<OrderedText> renderText = List.of();
        private DoubleList renderTextWidths = new DoubleArrayList();
        private int renderTextHeight = 0;

        public Instance(RawLabel widget) {
            super(widget);
        }

        @Override
        public void setWidget(RawLabel widget) {
            if (Objects.equals(this.widget.style, widget.style)
                && this.widget.softWrap == widget.softWrap
                && Objects.equals(this.widget.text, widget.text)) {
                return;
            }

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        protected List<OrderedText> wrapText(TextRenderer textRenderer, int maxWidth) {
            var styledText = this.widget.text.copy().styled(textStyle -> textStyle.withParent(this.widget.style.textStyle()));
            return textRenderer.wrapLines(styledText, this.widget.softWrap ? maxWidth : Integer.MAX_VALUE);
        }

        protected TextMetrics layoutText(TextRenderer textRenderer, List<OrderedText> lines) {
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
            this.renderText = this.wrapText(textRenderer, (int) constraints.maxWidth());

            var metrics = this.layoutText(textRenderer, this.renderText);

            this.renderTextWidths = metrics.lineWidths();
            this.renderTextHeight = metrics.height();

            var size = Size.of(metrics.width, metrics.height).constrained(constraints);
            this.transform.setSize(size);
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            var renderer = this.host().client().textRenderer;
            return this.layoutText(renderer, this.wrapText(renderer, Integer.MAX_VALUE)).width;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            var renderer = this.host().client().textRenderer;
            return this.layoutText(renderer, this.wrapText(renderer, this.widget.softWrap ? (int) width : Integer.MAX_VALUE)).height;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.of(this.host().client().textRenderer.fontHeight - 2);
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {
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
    }

    public record TextMetrics(int width, int height, DoubleList lineWidths) {}
}
