package io.wispforest.owo.text;

import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface TextLanguage {
    Text getText(String key);
}
