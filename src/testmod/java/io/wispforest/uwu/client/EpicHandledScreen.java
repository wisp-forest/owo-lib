package io.wispforest.uwu.client;

import io.wispforest.owo.mixin.ui.SlotAccessor;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.uwu.EpicScreenHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class EpicHandledScreen extends BaseOwoHandledScreen<FlowLayout, EpicScreenHandler> {

    public EpicHandledScreen(EpicScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var frogeNbt = new NbtCompound();
        frogeNbt.putString("variant", "delightful:froge");

        var selectBox = Components.textBox(Sizing.fixed(40));
        selectBox.setTextPredicate(s -> s.matches("\\d*"));

        rootComponent.child(
                Components.texture(new Identifier("textures/gui/container/shulker_box.png"), 0, 0, 176, 166)
        ).child(
                Containers.draggable(
                        Sizing.content(), Sizing.content(),
                        Containers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(Components.label(Text.literal("froge :)"))
                                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                                        .positioning(Positioning.absolute(0, -9))
                                        .horizontalSizing(Sizing.fixed(100)))
                                .child(Components.entity(Sizing.fixed(100), EntityType.FROG, frogeNbt).scale(.75f).allowMouseRotation(true).tooltip(Text.literal(":)")))
                                .child(Containers.horizontalFlow(Sizing.fixed(100), Sizing.content())
                                        .child(Components.button(Text.of("✔"), button -> {
                                            this.enableSlot(Integer.parseInt(selectBox.getText()));
                                        }).tooltip(Text.literal("Enable")))
                                        .child(selectBox.margins(Insets.horizontal(3)).tooltip(Text.literal("Slot Index")))
                                        .child(Components.button(Text.of("❌"), button -> {
                                            this.disableSlot(Integer.parseInt(selectBox.getText()));
                                        }).tooltip(Text.literal("Disable"))).verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER))
                                .allowOverflow(true)
                ).alwaysOnTop(true).surface(Surface.DARK_PANEL).padding(Insets.of(5)).allowOverflow(true).positioning(Positioning.absolute(100, 100))
        ).surface(Surface.VANILLA_TRANSLUCENT).verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Screen.hasAltDown() && this.focusedSlot != null) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (Screen.hasAltDown() && this.focusedSlot != null) {
            var accessor = ((SlotAccessor) this.focusedSlot);
            accessor.owo$setX((int) Math.round(this.focusedSlot.x + deltaX));
            accessor.owo$setY((int) Math.round(this.focusedSlot.y + deltaY));
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
