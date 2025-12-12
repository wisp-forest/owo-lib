package io.wispforest.owo.mixin.extension.json5;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.util.DataExtensionUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static io.wispforest.owo.util.DataExtensionUtil.coerceJson;

@Mixin(FallbackResourceManager.class)
public abstract class FallbackResourceManagerMixin {

    @WrapMethod(method = "getResourceStack")
    private List<Resource> json5$getAllResources(Identifier id, Operation<List<Resource>> original) {
        var base = original.call(id);
        if (id.getPath().endsWith(".json")) original
            .call(id.withPath(id.getPath() + 5))
            .forEach(resource -> {
                if (DataExtensionUtil.JSON5_ENABLED_PACKS.contains(resource.source().packId())) {
                    base.add(new Resource(resource.source(), () -> coerceJson(resource.open())));
                }
            });
        return base;
    }

    @WrapWithCondition(
        method = "listResources",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/PackResources;listResources(Lnet/minecraft/server/packs/PackType;Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/server/packs/PackResources$ResourceOutput;)V"
        )
    )
    private boolean json5$findResources(
        PackResources instance,
        PackType resourceType,
        String namespace,
        String startingPath,
        PackResources.ResourceOutput resultConsumer,
        @Local(argsOnly = true) Predicate<Identifier> predicate
    ) {
        return !(predicate instanceof DataExtensionUtil.OptInIdentifierPredicate)
               || DataExtensionUtil.JSON5_ENABLED_PACKS.contains(instance.packId());
    }

    @WrapWithCondition(
        method = "listResourceStacks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;listPackResources(Lnet/minecraft/server/packs/resources/FallbackResourceManager$PackEntry;Ljava/lang/String;Ljava/util/function/Predicate;Ljava/util/Map;)V"
        )
    )
    private boolean json5$findAllResources(
        FallbackResourceManager instance,
        FallbackResourceManager.PackEntry pack,
        String startingPath,
        Predicate<Identifier> allowedPathPredicate,
        Map<?, ?> idToEntryList,
        @Local(argsOnly = true) Predicate<Identifier> predicate
    ) {
        return !(predicate instanceof DataExtensionUtil.OptInIdentifierPredicate)
               || pack.resources != null && DataExtensionUtil.JSON5_ENABLED_PACKS.contains(pack.resources.packId());
    }
}
