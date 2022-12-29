package io.wispforest.owo.ops;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

/**
 * A collection of common operations
 * for working with and stylizing {@link Text}
 */
public final class TextOps {

    private TextOps() {}

    /**
     * Appends the {@code text} onto the {@code prefix} without
     * modifying the siblings of either one
     *
     * @param prefix The prefix
     * @param text   The text to add onto the prefix
     * @return The combined text
     */
    public static MutableText concat(Text prefix, Text text) {
        return Text.empty().append(prefix).append(text);
    }

    /**
     * Creates a new {@link Text} with the specified color
     * already applied
     *
     * @param text  The text to create
     * @param color The color to use in {@code RRGGBB} format
     * @return The colored text, specifically a {@link LiteralTextContent}
     */
    public static MutableText withColor(String text, int color) {
        return Text.literal(text).setStyle(Style.EMPTY.withColor(color));
    }

    /**
     * Creates a new {@link Text} with the specified color
     * already applied
     *
     * @param text  The text to create
     * @param color The color to use in {@code RRGGBB} format
     * @return The colored text, specifically a {@link TranslatableTextContent}
     */
    public static MutableText translateWithColor(String text, int color) {
        return Text.translatable(text).setStyle(Style.EMPTY.withColor(color));
    }

    /**
     * Applies multiple {@link Formatting}s to the given String, with
     * each one after the first one beginning on a {@code §} symbol.
     * The amount of {@code §} symbols must equal the amount of
     * supplied formattings - 1
     *
     * @param text       The text to format, with optional format delimiters
     * @param formatting The formattings to apply
     * @return The formatted text
     */
    public static MutableText withFormatting(String text, Formatting... formatting) {
        var textPieces = text.split("§");
        if (formatting.length != textPieces.length) return withColor("unmatched format specifiers - this is a bug", 0xff007f);

        var textBase = Text.literal(textPieces[0]).formatted(formatting[0]);

        for (int i = 1; i < textPieces.length; i++) {
            textBase.append(Text.literal(textPieces[i]).formatted(formatting[i]));
        }

        return textBase;
    }

    /**
     * Applies multiple colors to the given String, with
     * each one after the first one beginning on a {@code §} symbol.
     * The amount of {@code §} symbols must equal the amount of
     * supplied colors - 1
     *
     * @param text   The text to colorize, with optional color delimiters
     * @param colors The colors to apply, in {@code RRGGBB} format
     * @return The colorized text
     * @see #color(Formatting)
     */
    public static MutableText withColor(String text, int... colors) {
        var textPieces = text.split("§");
        if (colors.length != textPieces.length) return withColor("unmatched color specifiers - this is a bug", 0xff007f);

        var textBase = withColor(textPieces[0], colors[0]);

        for (int i = 1; i < textPieces.length; i++) {
            textBase.append(withColor(textPieces[i], colors[i]));
        }

        return textBase;
    }

    /**
     * Determine the width of the given iterable of texts,
     * which is defined as the width of the widest text
     * in the iterable
     *
     * @param renderer The text renderer responsible for rendering
     *                 the text later on
     * @param texts    The texts to check
     * @return The width of the widest text in the collection
     */
    public static int width(TextRenderer renderer, Iterable<Text> texts) {
        int width = 0;
        for (var text : texts) width = Math.max(width, renderer.getWidth(text));
        return width;
    }

    /**
     * Determine the width of the given iterable of texts,
     * which is defined as the width of the widest text
     * in the iterable
     *
     * @param renderer The text renderer responsible for rendering
     *                 the text later on
     * @param texts    The texts to check
     * @return The width of the widest text in the collection
     */
    public static int widthOrdered(TextRenderer renderer, Iterable<OrderedText> texts) {
        int width = 0;
        for (var text : texts) width = Math.max(width, renderer.getWidth(text));
        return width;
    }

    /**
     * @return The color value associated with the given formatting
     * in {@code RRGGBB} format, or {@code 0} if there is none
     */
    public static int color(Formatting formatting) {
        return formatting.getColorValue() == null ? 0 : formatting.getColorValue();
    }

}
