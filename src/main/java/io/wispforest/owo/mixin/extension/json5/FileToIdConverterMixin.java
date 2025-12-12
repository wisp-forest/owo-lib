package io.wispforest.owo.mixin.extension.json5;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static io.wispforest.owo.util.DataExtensionUtil.OptInIdentifierPredicate;
import static io.wispforest.owo.util.DataExtensionUtil.coerceJson;

@Mixin(FileToIdConverter.class)
public abstract class FileToIdConverterMixin {

    @Shadow @Final private String extension;

    @WrapOperation(
        method = "listMatchingResources",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/ResourceManager;listResources(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;"
        )
    )
    private Map<Identifier, Resource> json5$findResources(
        ResourceManager instance,
        String directoryName,
        Predicate<Identifier> identifierPredicate,
        Operation<Map<Identifier, Resource>> original
    ) {
        var base = original.call(instance, directoryName, identifierPredicate);
        if (this.extension.equals(".json")) {
            original
                .call(instance, directoryName, OptInIdentifierPredicate.of(path -> path.getPath().endsWith(".json5")))
                .forEach((identifier, resource) -> base.put(
                    identifier,
                    new Resource(resource.source(), () -> coerceJson(resource.open()))
                ));
        }
        return base;
    }

    @WrapOperation(
        method = "listMatchingResourceStacks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/ResourceManager;listResourceStacks(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;"
        )
    )
    private Map<Identifier, List<Resource>> json5$findAllResources(
        ResourceManager instance,
        String directoryName,
        Predicate<Identifier> identifierPredicate,
        Operation<Map<Identifier, List<Resource>>> original
    ) {
        var base = original.call(instance, directoryName, identifierPredicate);
        if (this.extension.equals(".json")) {
            original
                .call(instance, directoryName, OptInIdentifierPredicate.of(path -> path.getPath().endsWith(".json5")))
                .forEach((identifier, resources) -> base
                    .computeIfAbsent(identifier, id -> new ArrayList<>())
                    .addAll(resources
                        .stream()
                        .map(resource -> new Resource(resource.source(), () -> coerceJson(resource.open())))
                        .toList()
                    )
                );
        }
        return base;
    }

    @WrapMethod(method = "fileToId")
    private Identifier json5$fixToResourceId(
        Identifier path, Operation<Identifier> original
    ) {
        if (this.extension.equals(".json") && path.getPath().endsWith(".json5"))
            path = path.withPath(path.getPath().substring(0, path.getPath().length() - 1));
        return original.call(path);
    }
}
