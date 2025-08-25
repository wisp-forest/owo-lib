package io.wispforest.owo.braid.core;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TextLayout {

    public static EditMetrics measure(TextRenderer textRenderer, String text, Style baseStyle, int maxWidth) {
        var lines = new ArrayList<Line>();

        textRenderer.getTextHandler().wrapLines(
            text,
            maxWidth,
            baseStyle,
            false,
            (style, start, end) -> lines.add(new Line(style, start, end))
        );

        if (text.endsWith("\n")) {
            lines.add(new Line(baseStyle, text.length(), text.length()));
        }

        if (lines.isEmpty()) {
            lines.add(new Line(baseStyle, 0, 0));
        }

        // ---

        var textWidth = 0;
        var textHeight = 0;
        var lineMetrics = new ArrayList<LineMetrics>();

        for (var line : lines) {
            var lineWidth = textRenderer.getWidth(line.substring(text));
            lineMetrics.add(new LineMetrics(line.beginIdx, line.endIdx, lineWidth));

            textWidth = Math.max(textWidth, lineWidth);
            textHeight += textRenderer.fontHeight;
        }

        return new EditMetrics(textWidth, textHeight, lineMetrics);
    }

    public record LineMetrics(int beginIdx, int endIdx, double width) {
        public String substring(String fullContent) {
            return fullContent.substring(this.beginIdx, this.endIdx);
        }
    }

    public record EditMetrics(int width, int height, List<LineMetrics> lineMetrics) {}

    private record Line(Style style, int beginIdx, int endIdx) {
        public Text substring(String fullContent) {
            return Text.literal(fullContent.substring(this.beginIdx, this.endIdx)).setStyle(this.style);
        }
    }
}
