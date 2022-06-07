package io.wispforest.owo.text;

import net.minecraft.text.TextContent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface CustomTextContent extends TextContent {
    CustomTextContentSerializer<?> serializer();
}
