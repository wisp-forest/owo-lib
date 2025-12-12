package io.wispforest.owo.mixin.text.stapi;

import io.wispforest.owo.text.TextLanguage;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.server.translations.api.language.ServerLanguage;
import xyz.nucleoid.server.translations.impl.language.SystemDelegatedLanguage;

@Pseudo
@Mixin(SystemDelegatedLanguage.class)
public abstract class SystemDelegatedLanguageFixin implements TextLanguage {
    @Final
    @Shadow private Language vanilla;

    @Shadow
    protected abstract ServerLanguage getSystemLanguage();

    @Override
    public Component getText(String key) {
        if (!(vanilla instanceof TextLanguage lang) || this.getSystemLanguage().serverTranslations().contains(key)) {
            return null;
        }

        return lang.getText(key);
    }
}
