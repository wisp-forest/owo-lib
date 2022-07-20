package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class DropdownComponent extends VerticalFlowLayout {

    public DropdownComponent(Sizing horizontalSizing) {
        super(horizontalSizing, Sizing.content());
        this.padding(Insets.of(2));
        this.allowOverflow(true);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        Drawer.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x77000000);
        Drawer.drawRectOutline(matrices, this.x, this.y, this.width, this.height, 0x77FFFFFF);
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
    }

    public DropdownComponent divider() {
        return (DropdownComponent) this.child(new Divider());
    }

    public DropdownComponent text(Text text) {
        return (DropdownComponent) this.child(Components.label(text).color(0xAFAFAF).margins(Insets.vertical(1)));
    }

    public DropdownComponent button(Text text, Consumer<DropdownComponent> onClick) {
        return (DropdownComponent) this.child(new Button(text, onClick));
    }

    public DropdownComponent checkbox(Text text, boolean state) {
        return (DropdownComponent) this.child(new Checkbox(text, state));
    }

    protected static class Divider extends BaseComponent {

        public Divider() {
            this.verticalSizing(Sizing.fixed(1));
            this.horizontalSizing(Sizing.fixed(1));
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            Drawer.fill(matrices,
                    this.x - 1,
                    this.y + this.height / 2,
                    this.x + this.parent.width() - this.parent.padding().get().horizontal() + 1,
                    this.y + this.height / 2 + 1,
                    0x77FFFFFF
            );
        }
    }

    protected static class Button extends LabelComponent {
        protected Consumer<DropdownComponent> onClick;

        protected Button(Text text, Consumer<DropdownComponent> onClick) {
            super(text);
            this.onClick = onClick;
            this.margins(Insets.vertical(1));
            this.cursorStyle(CursorStyle.HAND);
        }

        @Override
        public boolean onMouseDown(double mouseX, double mouseY, int button) {
            super.onMouseDown(mouseX, mouseY, button);

            this.onClick.accept((DropdownComponent) this.parent);
            UISounds.playButtonSound();

            return true;
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            if (this.isInBoundingBox(mouseX, mouseY)) {
                var margins = this.margins.get();
                Drawer.fill(matrices,
                        this.x - margins.top(),
                        this.y - 1,
                        this.x + this.parent.width() - this.parent.padding().get().horizontal() + 1,
                        this.y + this.height + margins.bottom(),
                        0x44FFFFFF
                );
            }

            super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        }
    }

    protected static class Checkbox extends Button {

        protected static final Identifier CHECKBOX_TEXTURE = new Identifier("owo", "textures/gui/dropdown_checkbox.png");
        protected boolean state;

        public Checkbox(Text text, boolean state) {
            super(text, dropdownComponent -> {});

            this.state = state;
            this.onClick = dropdownComponent -> this.state = !this.state;
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(matrices, mouseX, mouseY, partialTicks, delta);

            RenderSystem.setShaderTexture(0, CHECKBOX_TEXTURE);
            Drawer.drawTexture(matrices,
                    this.x + this.parent.width() - this.parent.padding().get().horizontal() - 10,
                    this.y,
                    this.state ? 16 : 0, 0,
                    9, 9,
                    32, 16
            );
        }

        @Override
        protected void applyHorizontalContentSizing(Sizing sizing) {
            super.applyHorizontalContentSizing(sizing);
            this.width += 17;
        }
    }
}
