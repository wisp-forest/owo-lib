package io.wispforest.owo.mixin;

import io.wispforest.owo.util.TagInjector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.tags.TagLoader;

@Mixin(TagLoader.class)
public class TagGroupLoaderMixin {

    @Shadow
    @Final
    private String dataType;

    @Inject(method = "loadTags", at = @At("TAIL"))
    public void injectValues(ResourceManager manager, CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir) {
        var map = cir.getReturnValue();

        TagInjector.ADDITIONS.forEach((location, entries) -> {
            if (!this.dataType.equals(location.type())) return;

            var list = map.computeIfAbsent(location.tagId(), id -> new ArrayList<>());
            entries.forEach(addition -> list.add(new TagLoader.EntryWithSource(addition, "owo")));
        });
    }

}
