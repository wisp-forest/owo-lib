package io.wispforest.owo.text;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiConsumer;
import net.minecraft.network.chat.Text;

@ApiStatus.Internal
public class LanguageAccess {
    public static BiConsumer<String, Text> textConsumer;
}
