package io.wispforest.owo.mixin.extension.json5;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static io.wispforest.owo.util.DataExtensionUtil.*;

@Mixin(ResourceFinder.class)
public abstract class ResourceFinderMixin {

    @Shadow @Final private String fileExtension;

    @WrapOperation(
        method = "findResources",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/resource/ResourceManager;findResources(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;"
        )
    )
    private Map<Identifier, Resource> json5$findResources(
        ResourceManager instance,
        String directoryName,
        Predicate<Identifier> identifierPredicate,
        Operation<Map<Identifier, Resource>> original
    ) {
        var base = original.call(instance, directoryName, identifierPredicate);
        if (this.fileExtension.equals(".json")) {
            original
                .call(instance, directoryName, OptInIdentifierPredicate.of(path -> path.getPath().endsWith(".json5")))
                .forEach((identifier, resource) -> base.put(
                    identifier,
                    new Resource(resource.getPack(), () -> coerceJson(resource.getInputStream()))
                ));
        }
        return base;
    }

    @WrapOperation(
        method = "findAllResources",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/resource/ResourceManager;findAllResources(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;"
        )
    )
    private Map<Identifier, List<Resource>> json5$findAllResources(
        ResourceManager instance,
        String directoryName,
        Predicate<Identifier> identifierPredicate,
        Operation<Map<Identifier, List<Resource>>> original
    ) {
        var base = original.call(instance, directoryName, identifierPredicate);
        if (this.fileExtension.equals(".json")) {
            original
                .call(instance, directoryName, OptInIdentifierPredicate.of(path -> path.getPath().endsWith(".json5")))
                .forEach((identifier, resources) -> base
                    .computeIfAbsent(identifier, id -> new ArrayList<>())
                    .addAll(resources
                        .stream()
                        .map(resource -> new Resource(resource.getPack(), () -> coerceJson(resource.getInputStream())))
                        .toList()
                    )
                );
        }
        return base;
    }

    @WrapMethod(method = "toResourceId")
    private Identifier json5$fixToResourceId(
        Identifier path, Operation<Identifier> original
    ) {
        if (this.fileExtension.equals(".json") && path.getPath().endsWith(".json5"))
            path = path.withPath(path.getPath().substring(0, path.getPath().length() - 1));
        return original.call(path);
    }
}
