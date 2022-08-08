package io.wispforest.owo.text;

import net.minecraft.text.TextContent;
import org.jetbrains.annotations.ApiStatus;

public interface CustomTextContent extends TextContent {
    CustomTextContentSerializer<?> serializer();
}
