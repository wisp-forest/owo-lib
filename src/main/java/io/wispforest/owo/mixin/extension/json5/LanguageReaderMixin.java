package io.wispforest.owo.mixin.extension.json5;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.util.DataExtensionUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.server.translations.impl.language.LanguageReader;

import java.util.Map;
import java.util.function.Predicate;

import static io.wispforest.owo.util.DataExtensionUtil.coerceJson;

@Mixin(LanguageReader.class)
public abstract class LanguageReaderMixin {

    @WrapOperation(
        method = "collectDataPackTranslations",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/ResourceManager;listResources(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;"
        )
    )
    private static Map<Identifier, Resource> json5$collectDataPackTranslations(
        ResourceManager instance,
        String s,
        Predicate<Identifier> identifierPredicate,
        Operation<Map<Identifier, Resource>> original
    ) {
        var base = original.call(instance, s, identifierPredicate);
        original.call(instance, s, DataExtensionUtil.OptInIdentifierPredicate.of(path -> path.getPath().endsWith(".json5")))
            .forEach((identifier, resource) -> base.putIfAbsent(
                identifier, new Resource(resource.source(), () -> coerceJson(resource.open()))
            ));
        return base;
    }
}
