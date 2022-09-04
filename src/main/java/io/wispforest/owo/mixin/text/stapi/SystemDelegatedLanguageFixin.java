package io.wispforest.owo.mixin.text.stapi;

import fr.catcore.server.translations.api.resource.language.ServerLanguage;
import fr.catcore.server.translations.api.resource.language.SystemDelegatedLanguage;
import io.wispforest.owo.text.TextLanguage;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(SystemDelegatedLanguage.class)
public abstract class SystemDelegatedLanguageFixin implements TextLanguage {
    @Shadow private Language vanilla;

    @Shadow
    protected abstract ServerLanguage getSystemLanguage();

    @Override
    public Text getText(String key) {
        if (!(vanilla instanceof TextLanguage lang) || this.getSystemLanguage().local().contains(key))
            return null;

        return lang.getText(key);
    }
}
