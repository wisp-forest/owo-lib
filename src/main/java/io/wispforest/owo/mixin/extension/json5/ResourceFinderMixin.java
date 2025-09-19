package io.wispforest.owo.mixin.extension.json5;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.wispforest.owo.util.DataExtensionUtil.*;

@Mixin(ResourceFinder.class)
public abstract class ResourceFinderMixin {

    @Shadow @Final private String directoryName;
    @Shadow @Final private String fileExtension;

    @WrapMethod(method = "findResources")
    private Map<Identifier, Resource> json5$findResources(
        ResourceManager resourceManager,
        Operation<Map<Identifier, Resource>> original
    ) {
        var base = original.call(resourceManager);
        if (this.fileExtension.equals(".json")) {
            new ResourceFinder(directoryName, ".json5")
                .findResources(resourceManager)
                .forEach((identifier, resource) -> base
                    .put(identifier, new Resource(resource.getPack(), () -> coerceJson(resource.getInputStream()))));
        }
        return base;
    }

    @WrapMethod(method = "findAllResources")
    private Map<Identifier, List<Resource>> json5$findAllResources(
        ResourceManager resourceManager,
        Operation<Map<Identifier, List<Resource>>> original
    ) {
        var base = original.call(resourceManager);
        if (this.fileExtension.equals(".json")) {
            new ResourceFinder(directoryName, ".json5")
                .findResources(resourceManager)
                .forEach((identifier, resource) -> base
                    .computeIfAbsent(identifier, id -> new ArrayList<>())
                    .add(new Resource(resource.getPack(), () -> coerceJson(resource.getInputStream()))));
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
