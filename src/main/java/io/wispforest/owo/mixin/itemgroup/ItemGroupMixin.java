package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemGroupBuilder;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.impl.OwoItemGroupImpl;
import io.wispforest.owo.itemgroup.impl.OwoItemGroupStateImpl;
import io.wispforest.owo.util.pond.OwoItemGroupExtension;
import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin implements OwoItemGroupExtension {

    @Unique
    @Nullable
    private OwoItemGroup owo$extension;

    @Unique
    @Nullable
    private OwoItemGroupBuilder owo$builder;

    @Override
    public OwoItemGroup owo$getExtension() {
        return owo$extension;
    }

    @Override
    public void attemptToBuildExtension() {
        if (owo$builder == null) return;

        var extension = ((OwoItemGroupBuilderAccessor) owo$builder).owo$build();

        owo$builder.initializationEvent().invoker().onInitialization(extension);

        owo$extension = extension;

        ((ItemGroupAccessor) this).owo$setEntryCollector(OwoItemGroupImpl.createCollector((ItemGroup) (Object) this));
    }

    @Inject(method = "updateEntries", at = @At("TAIL"))
    private void owo$handlePossibleExtensionSearchEntries(ItemGroup.DisplayContext context, CallbackInfo ci) {
        var group = (ItemGroup) (Object) this;

        var state = OwoItemGroupState.getState(group, context);

        if (state != null) {
            state.updateSearchEntries(group, context);
        }
    }

    @Override
    public void owo$setBuilder(OwoItemGroupBuilder builder) {
        this.owo$builder = builder;
    }

    @Override
    public OwoItemGroupBuilder owo$getBuilder() {
        return this.owo$builder;
    }

    @Inject(method = "hasStacks", at = @At(value = "HEAD"), cancellable = true)
    private void test(CallbackInfoReturnable<Boolean> cir) {
        if(owo$extension != null) {
            cir.setReturnValue(true);
        }
    }
}
