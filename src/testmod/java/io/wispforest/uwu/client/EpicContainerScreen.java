package io.wispforest.uwu.client;

import io.wispforest.owo.mixin.ui.SlotAccessor;
import io.wispforest.owo.ui.base.BaseOwoContainerScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.uwu.EpicMenu;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class EpicContainerScreen extends BaseOwoContainerScreen<FlowLayout, EpicMenu> {
    private LabelComponent numberLabel;


    public EpicContainerScreen(EpicMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var frogeNbt = new CompoundTag();
        frogeNbt.putString("variant", "delightful:froge");

        var selectBox = UIComponents.textBox(Sizing.fixed(40));
        selectBox.setFilter(s -> s.matches("\\d*"));

        rootComponent.child(
                UIComponents.texture(Identifier.parse("textures/gui/container/shulker_box.png"), 0, 0, 176, 166)
        ).child(
                UIContainers.draggable(
                        Sizing.content(), Sizing.content(),
                        UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(UIComponents.label(Component.literal("froge :)"))
                                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                                        .positioning(Positioning.absolute(0, -9))
                                        .horizontalSizing(Sizing.fixed(100)))
                                .child(UIComponents.entity(Sizing.fixed(100), EntityType.FROG, frogeNbt).scale(.75f).allowMouseRotation(true).tooltip(Component.literal(":)")))
                                .child(UIContainers.horizontalFlow(Sizing.fixed(100), Sizing.content())
                                        .child(UIComponents.button(Component.nullToEmpty("✔"), (ButtonComponent button) -> {
                                            var text = selectBox.getValue();
                                            if (text.isBlank()) return;
                                            try {
                                                this.enableSlot(Integer.parseInt(text));
                                            } catch (Exception e) {}
                                        }).tooltip(Component.literal("Enable")))
                                        .child(selectBox.margins(Insets.horizontal(3)).tooltip(Component.literal("Slot Index")))
                                        .child(UIComponents.button(Component.nullToEmpty("❌"), (ButtonComponent button) -> {
                                            var text = selectBox.getValue();
                                            if (text.isBlank()) return;
                                            try {
                                                this.disableSlot(Integer.parseInt(text));
                                            } catch (Exception e) {}
                                        }).tooltip(Component.literal("Disable"))).verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER))
                                .allowOverflow(true)
                ).surface(Surface.DARK_PANEL).padding(Insets.of(5)).allowOverflow(true).positioning(Positioning.absolute(100, 100))
        ).child(
                UIContainers.verticalScroll(Sizing.content(), Sizing.fill(50), UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(this.slotAsComponent(0).tooltip(Component.nullToEmpty("bruh")))
                        .child(UIComponents.box(Sizing.fixed(50), Sizing.fixed(35)).startColor(Color.RED).endColor(Color.BLUE).fill(true).tooltip(Component.literal("very very long tooltip")))
                        .child(this.slotAsComponent(1))
                        .child(UIComponents.box(Sizing.fixed(50), Sizing.fixed(35)).startColor(Color.BLUE).endColor(Color.RED).fill(true))
                        .child(this.slotAsComponent(2))
                        .child(UIComponents.box(Sizing.fixed(50), Sizing.fixed(35)).startColor(Color.RED).endColor(Color.BLUE).fill(true))
                        .child(this.slotAsComponent(3))
                        .child(UIComponents.box(Sizing.fixed(50), Sizing.fixed(35)).startColor(Color.BLUE).endColor(Color.RED).fill(true))
                ).positioning(Positioning.relative(75, 50)).surface(Surface.outline(0x77000000)).padding(Insets.of(1))
        ).surface(Surface.VANILLA_TRANSLUCENT).verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);

        rootComponent.child(
                (numberLabel = UIComponents.label(Component.literal(menu.epicNumber.get())))
                        .positioning(Positioning.absolute(0, 0))
        );

        menu.epicNumber.observe(value -> numberLabel.text(Component.literal(value)));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.hasAltDown() && this.hoveredSlot != null) {
            return false;
        }

        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            this.uiAdapter.rootComponent.child(UIContainers.overlay(UIComponents.label(Component.literal("a"))));
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (click.hasAltDown() && this.hoveredSlot != null) {
            var accessor = ((SlotAccessor) this.hoveredSlot);
            accessor.owo$setX((int) Math.round(this.hoveredSlot.x + deltaX));
            accessor.owo$setY((int) Math.round(this.hoveredSlot.y + deltaY));
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }
}
