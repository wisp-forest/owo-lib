package io.wispforest.owo.itemgroup.impl;

import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.gui.IconRenderRegistry;
import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import io.wispforest.owo.itemgroup.gui.OwoItemGroupRenderer;
import io.wispforest.owo.mixin.itemgroup.CreativeInventoryScreenAccessor;
import io.wispforest.owo.mixin.itemgroup.ScreenAccessor;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.util.CursorAdapter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class DefaultOwoItemGroupRenderer extends OwoItemGroupRenderer {

    public DefaultOwoItemGroupRenderer(OwoItemGroupState state){
        super(state);
    }

    private int mouseX = 0;
    private int mouseY = 0;
    private float delta = 0;

    private final CursorAdapter cursorAdapter = CursorAdapter.ofClientWindow();
    private final List<ItemGroupButtonWidget> currentButtons = new ArrayList<>();

    public void beforeRender(int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.delta = delta;
    }

    public boolean renderBackground(DrawContext context, int x, int y, Consumer<Identifier> textureAdjuster) {
        var extension = getExtension();

        if (extension != null && extension.backgroundTexture() != null) {
            textureAdjuster.accept(extension.backgroundTexture());
        }

        return false;
    }

    @Override
    public boolean renderPageButtons(DrawContext context, int x, int y, Consumer<Identifier> textureAdjuster) {
        var extension = getExtension();

        if (extension != null && extension.pageButtonTexture() != null) {
            textureAdjuster.accept(extension.pageButtonTexture());
        }

        return false;
    }

    public boolean renderScrollbar(DrawContext context, int x, int y, boolean hasScrollbar, Consumer<Identifier> textureAdjuster) {
        var extension = getExtension();

        if (extension != null && extension.scrollerTextures() != null) {
            textureAdjuster.accept(extension.scrollerTextures().getTexture(hasScrollbar));
        }

        return false;
    }

    public boolean renderTab(DrawContext context, int x, int y, ItemGroup group, Consumer<Identifier> textureAdjuster) {
        var extension = OwoItemGroup.get(group);

        if (extension != null && extension.tabTextures() != null) {
            textureAdjuster.accept(extension.tabTextures().getTexture(group, selectedItemGroup()));
        }

        return false;
    }

    public boolean renderIcon(DrawContext context, int x, int y) {
        var extension = getExtension();

        if (extension != null) {
            return IconRenderRegistry.renderIcon(extension.icon(), context, x, y, mouseX, mouseY, delta);
        }

        return false;
    }

    public boolean renderTitle(DrawContext context, int x, int y, Text text, Consumer<Text> originalDraw) {
        if (state() != null) text = state().getDisplayName(text);

        originalDraw.accept(text);

        return false;
    }

    protected void init(int x, int y, CreativeInventoryScreen screen) {
        var state = this.state();

        var extension = state.getExtension();

        int tabRootY = y;

        final var tabStackHeight = extension.tabStackHeight();
        tabRootY -= 13 * (tabStackHeight - 4);

        if (extension.getTabs().size() > 1) {
            var tabs = extension.getTabs();

            for (int tabIdx = 0; tabIdx < tabs.size(); tabIdx++) {
                var tab = tabs.get(tabIdx);

                int xOffset = x - 27 - (tabIdx / tabStackHeight) * 26;
                int yOffset = tabRootY + 10 + (tabIdx % tabStackHeight) * 30;

                var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, 24, tab, createTabSelectAction(state, tabIdx, screen))
                    .setupTooltip(extension.allowMultiSelect());

                if (state.isTabSelected(tabIdx)) tabButton.isSelected = true;

                this.currentButtons.add(tabButton);
                screen.addDrawableChild(tabButton);
            }
        }

        final var buttonStackHeight = extension.buttonStackHeight();
        tabRootY = y - 13 * (buttonStackHeight - 4);

        var buttons = extension.getButtons();
        for (int i = 0; i < buttons.size(); i++) {
            var buttonDefinition = buttons.get(i);

            int xOffset = x + 198 + (i / buttonStackHeight) * 26;
            int yOffset = tabRootY + 10 + (i % buttonStackHeight) * 30;

            var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, 0, buttonDefinition, __ -> buttonDefinition.action().run());

            this.currentButtons.add(tabButton);
            screen.addDrawableChild(tabButton);
        }
    }

    @Override
    protected void closed(CreativeInventoryScreen screen) {
        this.currentButtons.forEach(((ScreenAccessor) screen)::owo$remove);
        this.currentButtons.clear();
    }

    public Stream<PositionedRectangle> getExclusionZones(int x, int y) {
        var extension = OwoItemGroup.get(CreativeInventoryScreenAccessor.owo$getSelectedTab());

        if (extension == null) return Stream.empty();

        var rectangles = Stream.<PositionedRectangle>builder();

        if (!extension.getTabs().isEmpty()) {
            var tabStackHeight = extension.tabStackHeight();
            var tabRootY = y - (13 * (tabStackHeight - 4));

            for (int i = 0; i < extension.getTabs().size(); i++) {
                int xOffset = x - 27 - (i / tabStackHeight) * 26;
                int yOffset = tabRootY + 10 + (i % tabStackHeight) * 30;

                rectangles.add(PositionedRectangle.of(xOffset, yOffset, 24, 24));
            }
        }

        if (!extension.getButtons().isEmpty()) {
            var stackHeight = extension.buttonStackHeight();
            var buttonRootY = y - (13 * (stackHeight - 4));

            for (int i = 0; i < extension.getButtons().size(); i++) {
                int xOffset = x + 198 + (i / stackHeight) * 26;
                int yOffset = buttonRootY + 10 + (i % stackHeight) * 30;
                rectangles.add(PositionedRectangle.of(xOffset, yOffset, 24, 24));
            }
        }

        return rectangles.build();
    }

    public void afterRender() {
        boolean anyButtonHovered = false;

        if (state() != null) {
            for (var button : this.currentButtons) {
                if (!button.isHovered()) continue;

                anyButtonHovered = true;

                break;
            }
        }

        this.cursorAdapter.applyStyle(anyButtonHovered ? CursorStyle.HAND : CursorStyle.NONE);
    }

    public void dispose(){
        cursorAdapter.dispose();
    }
}
