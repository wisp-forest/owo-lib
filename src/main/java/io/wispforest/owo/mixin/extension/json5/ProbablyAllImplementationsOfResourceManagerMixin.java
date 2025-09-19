package io.wispforest.owo.mixin.extension.json5;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

import static io.wispforest.owo.util.DataExtensionUtil.coerceJson;

@Mixin(
    {
        NamespaceResourceManager.class,
        ReloadableResourceManagerImpl.class,
        LifecycledResourceManagerImpl.class
    }
)
public abstract class ProbablyAllImplementationsOfResourceManagerMixin {

    @WrapMethod(method = "getAllResources")
    private List<Resource> json5$getAllResources(Identifier id, Operation<List<Resource>> original) {
        var base = original.call(id);
        if (id.getPath().endsWith(".json")) original
            .call(id.withPath(id.getPath() + "5"))
            .forEach(resource -> base.add(new Resource(
                resource.getPack(),
                () -> coerceJson(resource.getInputStream())
            )));
        return base;
    }
}
