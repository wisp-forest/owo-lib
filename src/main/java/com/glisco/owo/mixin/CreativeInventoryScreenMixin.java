package com.glisco.owo.mixin;

import com.glisco.owo.itemgroup.OwoItemGroup;
import com.glisco.owo.itemgroup.gui.ItemGroupButtonWidget;
import com.glisco.owo.util.OwoCreativeInventoryScreenExtensions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> implements OwoCreativeInventoryScreenExtensions {

    @Shadow
    private static int selectedTab;

    @Shadow
    @Final
    private static Identifier TEXTURE;

    @Unique
    private final List<ItemGroupButtonWidget> tabButtons = new ArrayList<>();

    @Unique
    private OwoItemGroup owoGroup = null;

    @ModifyArg(method = "drawBackground", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V", ordinal = 1))
    private Identifier injectCustomGroupTexture(Identifier original) {
        if (!(ItemGroup.GROUPS[selectedTab] instanceof OwoItemGroup owoGroup) || owoGroup.getCustomTexture() == null) return original;
        return owoGroup.getCustomTexture();
    }

    @Inject(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;isSpecial()Z"))
    private void injectCustomTabTexture(MatrixStack matrices, ItemGroup group, CallbackInfo ci) {
        if (!(group instanceof OwoItemGroup owoGroup) || owoGroup.getCustomTexture() == null) return;
        this.owoGroup = owoGroup;
        RenderSystem.setShaderTexture(0, owoGroup.getCustomTexture());
    }

    @ModifyArg(method = "renderTabIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"), index = 3)
    private int injectCustomTabTextureLocation(int original) {
        if (owoGroup == null) return original;
        return owoGroup.getColumn() == 0 ? 195 : 223;
    }

    @Inject(method = "renderTabIcon", at = @At("RETURN"))
    private void restoreTabTexture(MatrixStack matrices, ItemGroup group, CallbackInfo ci) {
        if (owoGroup == null) return;
        this.owoGroup = null;
        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    @ModifyArg(method = "drawForeground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"))
    private Text injectTabNameAsTitle(Text original) {
        if (!(ItemGroup.GROUPS[selectedTab] instanceof OwoItemGroup owoGroup) || !owoGroup.shouldDisplayTabNamesAsTitle()) return original;
        return tabButtons.get(owoGroup.getSelectedTabIndex()).getMessage();
    }

    @Inject(at = @At("HEAD"), method = "setSelectedTab(Lnet/minecraft/item/ItemGroup;)V")
    private void setSelectedTab(ItemGroup group, CallbackInfo ci) {
        tabButtons.forEach(this::remove);
        tabButtons.clear();

        if (group instanceof OwoItemGroup owoGroup) {

            int tabRootY = this.y;

            final int stackHeight = owoGroup.getStackHeight();
            if (stackHeight > 4) tabRootY -= 13 * (stackHeight - 4);

            if (owoGroup.tabs.size() > 1) {
                for (int i = 0; i < owoGroup.tabs.size(); i++) {
                    var tab = owoGroup.tabs.get(i);

                    int xOffset = this.x - 27 - (i / stackHeight) * 26;
                    int yOffset = tabRootY + 10 + (i % stackHeight) * 30;

                    var tabButton = new ItemGroupButtonWidget(xOffset, yOffset, false, tab, group.getName(), createSelectAction(this, owoGroup, i));

                    if (i == owoGroup.getSelectedTabIndex()) tabButton.isSelected = true;

                    tabButtons.add(tabButton);
                    this.addDrawableChild(tabButton);
                }
            }

            var buttons = owoGroup.getButtons();
            for (int i = 0; i < buttons.size(); i++) {
                var button = buttons.get(i);

                int xOffset = this.x + 198 + (i / stackHeight) * 26;
                int yOffset = tabRootY + 10 + (i % stackHeight) * 30;

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