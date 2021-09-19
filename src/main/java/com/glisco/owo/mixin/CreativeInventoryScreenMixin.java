package com.glisco.owo.mixin;

import com.glisco.owo.group.ItemGroupTabWidget;
import com.glisco.owo.group.TabbedItemGroup;
import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    private final List<ItemGroupTabWidget> tabButtons = Lists.newArrayList();
    private ItemGroupTabWidget selectedSubtab;

    @Inject(at = @At("HEAD"), method = "setSelectedTab(Lnet/minecraft/item/ItemGroup;)V")
    private void setSelectedTab(ItemGroup group, CallbackInfo cbi) {
        for (var button : tabButtons) {
            this.remove(button);
        }
        tabButtons.clear();

        if (group instanceof TabbedItemGroup tabbedGroup) {
            if (!tabbedGroup.hasInitialized()) {
                tabbedGroup.initialize();
            }

            int i = 0;
            for(var tab : tabbedGroup.getTabs()) {
                var selectTab = i;
                var flipTab = i > 3;
                var xOffset = flipTab ? (this.x + 191) : (this.x - 29);
                var yOffset = flipTab ? (this.y + 12) + ((i - 4) * 30) : (this.y + 12) + (i * 30);
                var tabWidget = new ItemGroupTabWidget(xOffset, yOffset, flipTab, tab, (button)-> {
                    tabbedGroup.setSelectedTab(selectTab);
                    MinecraftClient.getInstance().openScreen(this);
                    ((ItemGroupTabWidget) button).isSelected = true;
                    selectedSubtab = (ItemGroupTabWidget) button;
                });

                if(i == tabbedGroup.getSelectedTabIndex()) {
                    selectedSubtab = tabWidget;
                    tabWidget.isSelected = true;
                }

                tabButtons.add(tabWidget);
                this.addDrawableChild(tabWidget);
                i++;
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo cbi) {
        tabButtons.forEach(b -> {
            if (b.isHovered()) {
                renderTooltip(matrixStack, b.getMessage(), mouseX, mouseY);
            }
        });
    }

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }
}