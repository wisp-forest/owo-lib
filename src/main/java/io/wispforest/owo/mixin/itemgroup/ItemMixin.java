package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.util.pond.OwoItemExtensions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Mixin(Item.class)
public class ItemMixin implements OwoItemExtensions {

    protected @Nullable Supplier<@Nullable ? extends ItemGroup> owo$group = () -> null;

    @Unique
    private int owo$tab = 0;

    @Unique
    private BiConsumer<Item, ItemGroup.Entries> owo$stackGenerator;

    @Unique
    private boolean owo$trackUsageStat = false;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void grabTab(Item.Settings settings, CallbackInfo ci) {
        this.owo$tab = settings.tab();
        this.owo$stackGenerator = settings.stackGenerator();
        this.owo$group = settings.groupSupplier();
        this.owo$trackUsageStat = settings.shouldTrackUsageStat();
    }

    @Override
    public int owo$tab() {
        return owo$tab;
    }

    @Override
    public BiConsumer<Item, ItemGroup.Entries> owo$stackGenerator() {
        return this.owo$stackGenerator != null ? this.owo$stackGenerator : OwoItemGroup.DEFAULT_STACK_GENERATOR;
    }

    @Override
    public void owo$setGroup(Supplier<ItemGroup> group) {
        this.owo$group = group;
    }

    @Override
    public @Nullable ItemGroup owo$group() {
        return this.owo$group != null ? this.owo$group.get() : null;
    }

    @Override
    public boolean owo$shouldTrackUsageStat() {
        return this.owo$trackUsageStat;
    }
}
