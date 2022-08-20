package io.wispforest.owo.text;

import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

public interface TextLanguage {
    Text getText(String key);
}
