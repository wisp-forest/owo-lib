package io.wispforest.owo.mixin.text;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;

@Mixin(TranslatableContents.class)
public interface TranslatableContentsAccessor {
    @Invoker("decomposeTemplate")
    void owo$decomposeTemplate(String translation, Consumer<FormattedText> partsConsumer);
}
