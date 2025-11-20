package io.wispforest.owo.itemgroup.core;

import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public record TabTextures(Identifier topSelected, Identifier topSelectedFirstColumn, Identifier topUnselected,
                          Identifier bottomSelected, Identifier bottomSelectedFirstColumn, Identifier bottomUnselected) {

    public Identifier getTexture(ItemGroup group, ItemGroup selectedGroup) {
        return group.getRow() == ItemGroup.Row.TOP
            ? selectedGroup == group ? group.getColumn() == 0 ? this.topSelectedFirstColumn() : this.topSelected() : this.topUnselected()
            : selectedGroup == group ? group.getColumn() == 0 ? this.bottomSelectedFirstColumn() : this.bottomSelected() : this.bottomUnselected();
    }
}
