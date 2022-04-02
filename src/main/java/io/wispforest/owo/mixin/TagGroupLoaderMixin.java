package io.wispforest.owo.mixin;

import io.wispforest.owo.util.TagInjector;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(TagGroupLoader.class)
public class TagGroupLoaderMixin {

    @Shadow
    @Final
    private String dataType;

    @Inject(method = "loadTags", at = @At("TAIL"))
    public void injectValues(ResourceManager manager, CallbackInfoReturnable<Map<Identifier, Tag.Builder>> cir) {
        var map = cir.getReturnValue();

        TagInjector.ADDITIIONS.forEach((location, entries) -> {
            if (!this.dataType.equals(location.type())) return;

            var builder = map.computeIfAbsent(location.tagId(), id -> new Tag.Builder());
            entries.forEach(addition -> builder.add(addition, "owo"));
        });
    }

}
