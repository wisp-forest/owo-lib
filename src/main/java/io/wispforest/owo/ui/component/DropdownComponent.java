package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class DropdownComponent extends HorizontalFlowLayout {

    protected static final Identifier ICONS_TEXTURE = new Identifier("owo", "textures/gui/dropdown_icons.png");
    protected final EntryList entries;
    protected boolean requiresHover = false;

    protected DropdownComponent(Sizing horizontalSizing) {
        super(Sizing.content(), Sizing.content());

        this.entries = new EntryList(horizontalSizing);
        this.child(this.entries);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        if (this.requiresHover && !this.isInBoundingBox(mouseX, mouseY)) {
            this.queue(() -> {
                this.requiresHover(false);
                this.parent.removeChild(this);
            });
        }
    }

    public DropdownComponent divider() {
        this.entries.child(new Divider());
        return this;
    }

    public DropdownComponent text(Text text) {
        this.entries.child(Components.label(text).color(0xAFAFAF));
        return this;
    }

    public DropdownComponent button(Text text, Consumer<DropdownComponent> onClick) {
        this.entries.child(new Button(text, onClick));
        return this;
    }

    public DropdownComponent checkbox(Text text, boolean state) {
        this.entries.child(new Checkbox(text, state));
        return this;
    }

    public DropdownComponent nested(Text text, Sizing horizontalSizing, Consumer<DropdownComponent> builder) {
        var nested = new DropdownComponent(horizontalSizing);
        builder.accept(nested);
        this.entries.child(new NestEntry(text, nested));
        return this;
    }

    @Override
    public FlowLayout removeChild(Component child) {
        if (child == this.entries) {
            this.queue(() -> {
                this.requiresHover(false);
                this.parent.removeChild(this);
            });
        }
        return super.removeChild(child);
    }

    protected static void drawIconFromTexture(MatrixStack matrices, ParentComponent dropdown, int y, int u, int v) {
        RenderSystem.setShaderTexture(0, ICONS_TEXTURE);
        Drawer.drawTexture(matrices,
                dropdown.x() + dropdown.width() - dropdown.padding().get().right() - 10,
                y,
                u, v,
                9, 9,
                32, 32
        );
    }

    public boolean requiresHover() {
        return this.requiresHover;
    }

    public DropdownComponent requiresHover(boolean requiresHover) {
        this.requiresHover = requiresHover;
        return this;
    }

    public static class EntryList extends VerticalFlowLayout {

        protected EntryList(Sizing horizontalSizing) {
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

    protected static class NestEntry extends LabelComponent {

        private final DropdownComponent child;

        protected NestEntry(Text text, DropdownComponent child) {
            super(text);
            this.child = child;

            this.mouseEnter().subscribe(() -> {
                final var dropdown = (DropdownComponent) this.parent.parent();
                child.margins(Insets.top(this.y - dropdown.y));

                dropdown.queue(() -> {
                    dropdown.removeChild(child);
                    dropdown.child(child);
                });
            });
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(matrices, mouseX, mouseY, partialTicks, delta);
            drawIconFromTexture(matrices, this.parent, this.y, 0, 16);

            this.child.requiresHover(!PositionedRectangle.of(this.x, this.y, this.parent.width(), this.height).isInBoundingBox(mouseX, mouseY));
        }

        @Override
        protected void applyHorizontalContentSizing(Sizing sizing) {
            super.applyHorizontalContentSizing(sizing);
            this.width += 17;
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

            this.onClick.accept((DropdownComponent) this.parent.parent());
            this.playInteractionSound();

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

        protected void playInteractionSound() {
            UISounds.playButtonSound();
        }
    }

    protected static class Checkbox extends Button {

        protected boolean state;

        public Checkbox(Text text, boolean state) {
            super(text, dropdownComponent -> {});

            this.state = state;
            this.onClick = dropdownComponent -> this.state = !this.state;
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(matrices, mouseX, mouseY, partialTicks, delta);
            drawIconFromTexture(matrices, this.parent, this.y, this.state ? 16 : 0, 0);
        }

        @Override
        protected void applyHorizontalContentSizing(Sizing sizing) {
            super.applyHorizontalContentSizing(sizing);
            this.width += 17;
        }

        @Override
        protected void playInteractionSound() {
            UISounds.playInteractionSound();
        }
    }
}
