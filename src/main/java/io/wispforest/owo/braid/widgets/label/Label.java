package io.wispforest.owo.braid.widgets.label;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public class Label extends LeafInstanceWidget {

    public final LabelStyle style;
    public final boolean softWrap;
    public final Text text;

    public Label(LabelStyle style, boolean softWrap, Text text) {
        this.style = style;
        this.softWrap = softWrap;
        this.text = text;
    }

    public Label(Text text) {
        this(LabelStyle.DEFAULT, true, text);
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<Label> {

        private List<OrderedText> renderText = List.of();
        private DoubleList renderTextWidths = new DoubleArrayList();
        private int renderTextHeight = 0;

        public Instance(Label widget) {
            super(widget);
        }

        @Override
        public void setWidget(Label widget) {
            if (this.widget.softWrap == widget.softWrap
                && Objects.equals(this.widget.text, widget.text)) {
                return;
            }

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var textRenderer = this.host().client().textRenderer;

            this.renderText = textRenderer.wrapLines(this.widget.text, this.widget.softWrap ? (int) constraints.maxWidth() : Integer.MAX_VALUE);

            var textWidth = 0;
            var textHeight = 0;
            var lineWidths = new DoubleArrayList();

            for (var line : this.renderText) {
                var lineWidth = textRenderer.getWidth(line);
                lineWidths.add(lineWidth);

                textWidth = Math.max(textWidth, lineWidth);
                textHeight += textRenderer.fontHeight;
            }

            this.renderTextWidths = lineWidths;
            this.renderTextHeight = textHeight;

            var size = Size.of(textWidth, textHeight).constrained(constraints);
            this.transform.setSize(size);
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
}
