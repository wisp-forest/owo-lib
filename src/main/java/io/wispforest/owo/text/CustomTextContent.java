package io.wispforest.owo.text;

import net.minecraft.text.TextContent;

public interface CustomTextContent extends TextContent {
    CustomTextContentSerializer<?> serializer();
}
