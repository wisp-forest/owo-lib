package io.wispforest.owo.itemgroup.gui;

import io.wispforest.owo.itemgroup.base.ButtonDefinition;
import io.wispforest.owo.itemgroup.core.ItemGroupButton;
import io.wispforest.owo.itemgroup.core.ItemGroupTab;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Internal
public class ItemGroupButtonWidget extends ButtonWidget {

    public boolean isSelected = false;
    private final ButtonDefinition definition;
    private final int baseU;

    public ItemGroupButtonWidget(int x, int y, int baseU, ButtonDefinition definition, Consumer<ItemGroupButtonWidget> onPress) {
        super(x, y, 24, 24, definition.tooltip(), button -> onPress.accept((ItemGroupButtonWidget) button), ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.baseU = baseU;
        this.definition = definition;

        setupTooltip();
    }

    public ItemGroupButtonWidget setupTooltip() {
        return setupTooltip(false);
    }

    public ItemGroupButtonWidget setupTooltip(boolean allowMultiSelect) {
        var texts = this.isTab() && allowMultiSelect
            ? List.of(this.getMessage(), Text.translatable("text.owo.itemGroup.select_hint"))
            : List.of(this.getMessage());

        var tooltip = Texts.join(texts, Text.of("\n"));

        setTooltip(Tooltip.of(tooltip));

        return this;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, this.definition.texture(),
            this.getX(), this.getY(),
            this.baseU, this.isSelected() || this.isSelected ? this.height : 0,
            this.width, this.height,
            48, 48
        );

        IconRenderRegistry.renderIcon(this.definition.icon(), context, this.getX() + 4, this.getY() + 4, mouseX, mouseY, delta);
    }

    public boolean isTab() {
        return this.definition instanceof ItemGroupTab;
    }
}
