package io.wispforest.owo.itemgroup;

import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.base.Icon;
import io.wispforest.owo.itemgroup.gui.ScrollerTextures;
import io.wispforest.owo.itemgroup.gui.TabTextures;
import io.wispforest.owo.itemgroup.impl.OwoItemGroupImpl;
import io.wispforest.owo.itemgroup.impl.OwoItemGroupStateImpl;
import io.wispforest.owo.mixin.itemgroup.ItemGroupAccessor;
import io.wispforest.owo.util.pond.OwoItemGroupExtension;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class OwoItemGroupBuilder {

    private final Event<ExtensionInitialization> onInitEvent = EventFactory.createArrayBacked(
        ExtensionInitialization.class,
        invokers -> extension -> {
            for (var invoker : invokers) invoker.onInitialization(extension);
        });

    private final RegistryKey<ItemGroup> id;
    private Supplier<Icon> iconSupplier = () -> Icon.EMPTY_INSTANCE;
    private int tabStackHeight = 4;
    private int buttonStackHeight = 4;
    private @Nullable Identifier backgroundTexture = null;
    private @Nullable ScrollerTextures scrollerTextures = null;
    private @Nullable TabTextures tabTextures = null;
    private boolean useDynamicTitle = true;
    private boolean allowMultiSelect = true;

    public OwoItemGroupBuilder(RegistryKey<ItemGroup> id) {
        this.id = id;
    }

    public static RegistryKey<ItemGroup> createItemGroup(Identifier id, Supplier<Icon> iconSupplier, Consumer<OwoItemGroupBuilder> buildHandler) {
        var group = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.%s.%s".formatted(id.getNamespace(), id.getPath())))
            .build();

        var builder = new OwoItemGroupBuilder(RegistryKey.of(RegistryKeys.ITEM_GROUP, id));

        builder.iconSupplier = iconSupplier;

        buildHandler.accept(builder);

        ((OwoItemGroupExtension) group).owo$setBuilder(builder);

        Registry.register(Registries.ITEM_GROUP, id, group);

        return RegistryKey.of(RegistryKeys.ITEM_GROUP, id);
    }

    public static void modifyItemGroup(RegistryKey<ItemGroup> itemGroupKey, Consumer<OwoItemGroupBuilder> buildHandler) {
        var group = Registries.ITEM_GROUP.getValueOrThrow(itemGroupKey);

        var builder = ((OwoItemGroupExtension) group).owo$getBuilder();

        if (builder == null) {
            var id = Registries.ITEM_GROUP.getKey(group).orElseThrow();

            builder = new OwoItemGroupBuilder(id);

            ((OwoItemGroupExtension) group).owo$setBuilder(builder);
        }

        buildHandler.accept(builder);
    }

    public Event<ExtensionInitialization> initializationEvent() {
        return this.onInitEvent;
    }

    public void initializer(ExtensionInitialization invoker) {
        this.onInitEvent.register(invoker);
    }

    public OwoItemGroupBuilder tabStackHeight(int tabStackHeight) {
        this.tabStackHeight = tabStackHeight;
        return this;
    }

    public OwoItemGroupBuilder buttonStackHeight(int buttonStackHeight) {
        this.buttonStackHeight = buttonStackHeight;
        return this;
    }

    public OwoItemGroupBuilder backgroundTexture(Identifier backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public OwoItemGroupBuilder scrollerTextures(ScrollerTextures scrollerTextures) {
        this.scrollerTextures = scrollerTextures;
        return this;
    }

    public OwoItemGroupBuilder tabTextures(TabTextures tabTextures) {
        this.tabTextures = tabTextures;
        return this;
    }

    public OwoItemGroupBuilder disableDynamicTitle() {
        this.useDynamicTitle = false;
        return this;
    }

    @ApiStatus.Internal
    private OwoItemGroupImpl build() {
        return new OwoItemGroupImpl(id, iconSupplier, backgroundTexture, scrollerTextures, tabTextures, tabStackHeight, buttonStackHeight, useDynamicTitle, allowMultiSelect);
    }

    public interface ExtensionInitialization {
        void onInitialization(OwoItemGroup extension);
    }
}
