package io.wispforest.owo.itemgroup.gui;

import net.minecraft.util.Identifier;

public record TabTextures(Identifier topSelected, Identifier topSelectedFirstColumn, Identifier topUnselected,
                          Identifier bottomSelected, Identifier bottomSelectedFirstColumn,
                          Identifier bottomUnselected) {
}
