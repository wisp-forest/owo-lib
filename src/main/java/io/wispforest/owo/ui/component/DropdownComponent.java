package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.ReflectionUtils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropdownComponent extends HorizontalFlowLayout {

    protected static final Identifier ICONS_TEXTURE = new Identifier("owo", "textures/gui/dropdown_icons.png");
    protected final VerticalFlowLayout entries;
    protected boolean closeWhenNotHovered = false;

    protected DropdownComponent(Sizing horizontalSizing) {
        super(Sizing.content(), Sizing.content());

        this.entries = Containers.verticalFlow(horizontalSizing, Sizing.content());
        this.entries.padding(Insets.of(1));
        this.entries.allowOverflow(true);
        this.entries.surface(Surface.flat(0xA7000000).and(Surface.outline(0xA7FFFFFF)));

        this.child(this.entries);
    }

    /**
     * Open a context menu at the given location in the given screen,
     * adjusting the position if needed to avoid overflowing screen space
     *
     * @param screen        The screen on which to operate
     * @param rootComponent The root component onto which to mount the dropdown
     * @param mountFunction The mounting function to use
     * @param mouseX        The x-coordinate at which to open the dropdown
     * @param mouseY        The y-coordinate at which to open the dropdown
     * @param builder       A function to add entries to the dropdown
     */
    public static <R extends ParentComponent> DropdownComponent openContextMenu(Screen screen, R rootComponent, BiConsumer<R, DropdownComponent> mountFunction, double mouseX, double mouseY, Consumer<DropdownComponent> builder) {
        var dropdown = new DropdownComponent(Sizing.content());
        builder.accept(dropdown);

        mountFunction.accept(rootComponent, dropdown);

        int xLocation = (int) mouseX - rootComponent.x();
        int yLocation = (int) mouseY - rootComponent.y();

        if (xLocation + dropdown.width() > screen.width) {
            xLocation -= xLocation + dropdown.width() - screen.width;
        }
        if (yLocation + dropdown.height() > screen.height) {
            yLocation -= yLocation + dropdown.height() - screen.height;
        }

        dropdown.positioning(Positioning.absolute(xLocation, yLocation));

        var dismounted = new MutableBoolean(false);
        ScreenMouseEvents.beforeMouseClick(screen).register((screen_, mouseX_, mouseY_, button) -> {
            if (dismounted.isTrue() || dropdown.isInBoundingBox(mouseX_, mouseY_)) return;

            rootComponent.removeChild(dropdown);
            dismounted.setTrue();
        });

        return dropdown;
    }

    @Override
    public ParentComponent surface(Surface surface) {
        return this.entries.surface(surface);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        if (this.closeWhenNotHovered && !this.isInBoundingBox(mouseX, mouseY)) {
            this.queue(() -> {
                this.closeWhenNotHovered(false);
                this.parent.removeChild(this);
            });
        }
    }

    @Override
    public void layout(Size space) {
        super.layout(space);

        var entries = this.entries.children();
        for (int i = 0; i < entries.size(); i++) {
            if (!(entries.get(i) instanceof ResizeableComponent sizeable)) continue;
            sizeable.setWidth(this.entries.width() - this.entries.padding().get().horizontal());
        }
    }

    public DropdownComponent divider() {
        this.entries.child(new Divider());
        return this;
    }

    public DropdownComponent text(Text text) {
        this.entries.child(Components.label(text).color(Color.ofFormatting(Formatting.GRAY)).margins(Insets.of(2)));
        return this;
    }

    public DropdownComponent button(Text text, Consumer<DropdownComponent> onClick) {
        this.entries.child(new Button(this, text, onClick).margins(Insets.of(2)));
        return this;
    }

    public DropdownComponent checkbox(Text text, boolean state, Consumer<Boolean> onClick) {
        this.entries.child(new Checkbox(this, text, state, onClick).margins(Insets.of(2)));
        return this;
    }

    public DropdownComponent nested(Text text, Sizing horizontalSizing, Consumer<DropdownComponent> builder) {
        var nested = new DropdownComponent(horizontalSizing);
        builder.accept(nested);
        this.entries.child(new NestEntry(this, text, nested).margins(Insets.of(2)));
        return this;
    }

    @Override
    public FlowLayout removeChild(Component child) {
        if (child == this.entries) {
            this.queue(() -> {
                this.closeWhenNotHovered(false);
                this.parent.removeChild(this);
            });
        }
        return super.removeChild(child);
    }

    protected static void drawIconFromTexture(MatrixStack matrices, ParentComponent dropdown, int y, int u, int v) {
        RenderSystem.setShaderTexture(0, ICONS_TEXTURE);
        Drawer.drawTexture(matrices,
                dropdown.x() + dropdown.width() - dropdown.padding().get().right() - 10, y,
                u, v,
                9, 9,
                32, 32
        );
    }

    @Deprecated(forRemoval = true)
    public DropdownComponent requiresHover(boolean requiresHover) {
        Owo.debugWarn(Owo.LOGGER, "Dropdown property 'closeWhenNotHovered' was modified via deprecated method 'requiresHover' by {}", ReflectionUtils.getCallingClassName(2));
        return this.closeWhenNotHovered(requiresHover);
    }

    @Deprecated(forRemoval = true)
    public boolean requiresHover() {
        Owo.debugWarn(Owo.LOGGER, "Dropdown property 'closeWhenNotHovered' was queried via deprecated method 'requiresHover' by {}", ReflectionUtils.getCallingClassName(2));
        return this.closeWhenNotHovered();
    }

    public DropdownComponent closeWhenNotHovered(boolean closeWhenNotHovered) {
        this.closeWhenNotHovered = closeWhenNotHovered;
        return this;
    }

    public boolean closeWhenNotHovered() {
        return this.closeWhenNotHovered;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "entries", Function.identity(), this::parseAndApplyEntries);

        UIParsing.apply(children, "requires-hover", UIParsing::parseBool, this::requiresHover);
        UIParsing.apply(children, "close-when-not-hovered", UIParsing::parseBool, this::closeWhenNotHovered);
    }

    protected void parseAndApplyEntries(Element container) {
        for (var node : UIParsing.allChildrenOfType(container, Node.ELEMENT_NODE)) {
            var entry = (Element) node;

            switch (entry.getNodeName()) {
                case "divider" -> this.divider();
                case "text" -> this.text(UIParsing.parseText(entry));
                case "button" -> {
                    var children = UIParsing.childElements(entry);
                    UIParsing.expectChildren(entry, children, "text");

                    var text = UIParsing.parseText(children.get("text"));
                    this.button(text, dropdownComponent -> {});
                }
                case "checkbox" -> {
                    var children = UIParsing.childElements(entry);
                    UIParsing.expectChildren(entry, children, "text", "checked");

                    var text = UIParsing.parseText(children.get("text"));
                    var checked = UIParsing.parseBool(children.get("checked"));

                    this.checkbox(text, checked, aBoolean -> {});
                }
                case "nested" -> {
                    var text = entry.getAttribute("translate").equals("true")
                            ? Text.translatable(entry.getAttribute("name"))
                            : Text.literal(entry.getAttribute("name"));
                    this.nested(text, Sizing.content(), dropdownComponent -> dropdownComponent.parseAndApplyEntries(entry));
                }
            }
        }
    }

    protected interface ResizeableComponent {
        void setWidth(int width);
    }

    protected static class Divider extends BaseComponent implements ResizeableComponent {

        public Divider() {
            this.sizing(Sizing.fixed(1));
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            var margins = this.margins.get();
            Drawer.fill(matrices,
                    this.x - margins.left(),
                    this.y - margins.top(),
                    this.x + this.width + margins.right(),
                    this.y + this.height + margins.bottom(),
                    0xA7FFFFFF
            );
        }

        @Override
        public void setWidth(int width) {
            this.width = width;
        }
    }

    protected static class NestEntry extends LabelComponent {

        private final DropdownComponent child;

        protected NestEntry(DropdownComponent parentDropdown, Text text, DropdownComponent child) {
            super(text);
            this.child = child;

            this.mouseEnter().subscribe(() -> {
                child.margins(Insets.top(this.y - parentDropdown.y));

                parentDropdown.queue(() -> {
                    parentDropdown.removeChild(child);
                    parentDropdown.child(child);
                });
            });
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(matrices, mouseX, mouseY, partialTicks, delta);
            drawIconFromTexture(matrices, this.parent, this.y, 0, 16);

            this.child.closeWhenNotHovered(!PositionedRectangle.of(this.x, this.y, this.parent.width(), this.height).isInBoundingBox(mouseX, mouseY));
        }

        @Override
        protected int determineHorizontalContentSize(Sizing sizing) {
            return super.determineHorizontalContentSize(sizing) + 17;
        }
    }

    protected static class Button extends LabelComponent implements ResizeableComponent {

        protected final DropdownComponent parentDropdown;
        protected Consumer<DropdownComponent> onClick;

        protected Button(DropdownComponent parentDropdown, Text text, Consumer<DropdownComponent> onClick) {
            super(text);
            this.onClick = onClick;
            this.parentDropdown = parentDropdown;

            this.margins(Insets.vertical(1));
            this.cursorStyle(CursorStyle.HAND);
        }

        public void setWidth(int width) {
            this.width = width;
        }

        @Override
        public boolean onMouseDown(double mouseX, double mouseY, int button) {
            super.onMouseDown(mouseX, mouseY, button);

            this.onClick.accept(this.parentDropdown);
            this.playInteractionSound();

            return true;
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            if (this.isInBoundingBox(mouseX, mouseY)) {
                var margins = this.margins.get();
                Drawer.fill(matrices,
                        this.x - margins.left(),
                        this.y - margins.top(),
                        this.x + this.width + margins.right(),
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

        public Checkbox(DropdownComponent parentDropdown, Text text, boolean state, Consumer<Boolean> onClick) {
            super(parentDropdown, text, dropdownComponent -> {});

            this.state = state;
            this.onClick = dropdownComponent -> {
                this.state = !this.state;
                onClick.accept(this.state);
            };
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(matrices, mouseX, mouseY, partialTicks, delta);
            drawIconFromTexture(matrices, this.parent, this.y, this.state ? 16 : 0, 0);
        }

        @Override
        protected int determineHorizontalContentSize(Sizing sizing) {
            return super.determineHorizontalContentSize(sizing) + 17;
        }

        @Override
        protected void playInteractionSound() {
            UISounds.playInteractionSound();
        }
    }
}
