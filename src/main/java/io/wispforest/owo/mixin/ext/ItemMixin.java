package io.wispforest.owo.mixin.ext;

import io.wispforest.owo.ext.OwoItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin implements OwoItem {
}
