package io.wispforest.owo.mixin.itemgroup;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FabricItemGroup.class)
public interface FabricItemGroupAccessor {

    @Mutable
    @Accessor("identifier")
    void owo$setId(Identifier id);

    @Invoker("getText")
    static Text owo$getText(Identifier id) {
        throw new AssertionError();
    }

}
