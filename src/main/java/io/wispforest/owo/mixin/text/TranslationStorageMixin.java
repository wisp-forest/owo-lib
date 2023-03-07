package io.wispforest.owo.mixin.text;

import com.google.common.collect.ImmutableMap;
import io.wispforest.owo.text.LanguageAccess;
import io.wispforest.owo.text.TextLanguage;
import io.wispforest.owo.util.KawaiiUtil;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mixin(TranslationStorage.class)
public class TranslationStorageMixin implements TextLanguage {

    @Mutable
    @Shadow
    @Final
    private Map<String, String> translations;

    private static Map<String, Text> owo$buildingTextMap;

    private Map<String, Text> owo$textMap;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kawaii(Map<String, String> translations, boolean rightToLeft, CallbackInfo ci) {
        if (!Objects.equals(System.getProperty("owo.uwu"), "yes please")) return;

        var builder = ImmutableMap.<String, String>builder();
        translations.forEach((s, s2) -> builder.put(s, KawaiiUtil.uwuify(s2)));
        this.translations = builder.build();
    }

    @Inject(method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Z)Lnet/minecraft/client/resource/language/TranslationStorage;", at = @At("HEAD"))
    private static void initTextMap(ResourceManager resourceManager, List<LanguageDefinition> definitions, boolean leftToRight, CallbackInfoReturnable<TranslationStorage> cir) {
        owo$buildingTextMap = new HashMap<>();
        LanguageAccess.textConsumer = owo$buildingTextMap::put;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(Map<String, String> translations, boolean rightToLeft, CallbackInfo ci) {
        this.owo$textMap = owo$buildingTextMap;
        owo$buildingTextMap = null;
    }

    @Inject(method = "hasTranslation", at = @At("HEAD"), cancellable = true)
    private void hasTranslation(String key, CallbackInfoReturnable<Boolean> cir) {
        if (this.owo$textMap.containsKey(key))
            cir.setReturnValue(true);
    }

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void get(String key, String fallback, CallbackInfoReturnable<String> cir) {
        if (this.owo$textMap.containsKey(key))
            cir.setReturnValue(this.owo$textMap.get(key).getString());
    }

    @Override
    public Text getText(String key) {
        return this.owo$textMap.get(key);
    }
}
