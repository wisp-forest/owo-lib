package com.glisco.owo.mixin;

import com.glisco.owo.itemgroup.OwoItemGroup;
import com.glisco.owo.itemgroup.gui.ItemGroupButtonWidget;
import com.glisco.owo.util.OwoCreativeInventoryScreenExtensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> implements OwoCreativeInventoryScreenExtensions {

    @Unique
    private final List<ItemGroupButtonWidget> tabButtons = new ArrayList<>();

    @Inject(at = @At("HEAD"), method = "setSelectedTab(Lnet/minecraft/item/ItemGroup;)V")
    private void setSelectedTab(ItemGroup group, CallbackInfo ci) {
        tabButtons.forEach(this::remove);
        tabButtons.clear();

        if (group instanceof OwoItemGroup tabbedGroup) {

            if (tabbedGroup.tabs.size() > 1) {
                for (int i = 0; i < tabbedGroup.tabs.size(); i++) {
                    var tab = tabbedGroup.tabs.get(i);

                    int xOffset = this.x - 27 - (i / 4) * 26;
                    int yOffset = this.y + 10 + (i % 4) * 30;

                    var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, false, tab, group.getName(), createSelectAction(this, tabbedGroup, i));

                    if (i == tabbedGroup.getSelectedTabIndex()) tabButton.isSelected = true;

                    tabButtons.add(tabButton);
                    this.addDrawableChild(tabButton);
                }
            }

            var buttons = tabbedGroup.getButtons();
            for (int i = 0; i < buttons.size(); i++) {
                var button = buttons.get(i);

                int xOffset = this.x + 198 + (i / 4) * 26;
                int yOffset = this.y + 10 + (i % 4) * 30;

                var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, true, button, group.getName(), button1 -> button.action().run());

                tabButtons.add(tabButton);
                this.addDrawableChild(tabButton);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo cbi) {
        tabButtons.forEach(button -> {
            if (button.isHovered()) renderTooltip(matrixStack, button.getMessage(), mouseX, mouseY);
        });
    }

    @Override
    public int getRootX() {
        return this.x;
    }

    @Override
    public int getRootY() {
        return this.y;
    }

    @Unique
    private static ButtonWidget.PressAction createSelectAction(Screen targetScreen, OwoItemGroup group, int targetTabIndex) {
        return button -> {
            group.setSelectedTab(targetTabIndex);
            MinecraftClient.getInstance().setScreen(targetScreen);
            ((ItemGroupButtonWidget) button).isSelected = true;
        };
    }

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }
}