package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.BaseComponent;
import io.wispforest.owo.ui.Drawer;
import io.wispforest.owo.ui.definitions.Sizing;
import net.minecraft.client.util.math.MatrixStack;

public class BoundingBoxComponent extends BaseComponent {

    public BoundingBoxComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        this.horizontalSizing.set(horizontalSizing);
        this.verticalSizing.set(verticalSizing);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        Drawer.drawRectOutline(matrices, this.x, this.y, this.width, this.height, 0xFF000000);
    }
}
