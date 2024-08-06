package io.wispforest.owo.mixin.itemgroup;

import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(CreativeModeTab.class)
public interface CreativeModeTabAccessor {

    @Accessor("displayItemsGenerator")
    CreativeModeTab.DisplayItemsGenerator owo$getDisplayItemsGenerator();

    @Mutable
    @Accessor("displayItemsGenerator")
    void owo$setDisplayItemsGenerator(CreativeModeTab.DisplayItemsGenerator generator);

    @Accessor("displayItemsSearchTab")
    void owo$setDisplayItemsSearchTab(Set<ItemStack> displayItemsSearchTab);

    @Mutable
    @Accessor("displayName")
    void owo$setDisplayName(Text displayName);

    @Mutable
    @Accessor("column")
    void owo$setColumn(int column);

    @Mutable
    @Accessor("row")
    void owo$setRow(CreativeModeTab.Row row);

    @Accessor("backgroundTexture")
    void owo$setBackgroundTexture(Identifier backgroundTexture);

    @Accessor
    static Identifier getDEFAULT_BACKGROUND() {
        throw new UnsupportedOperationException();
    }
}
