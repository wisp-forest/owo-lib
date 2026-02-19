package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.event.*;
import io.wispforest.owo.ui.inject.UIComponentStub;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.util.EventSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin implements UIComponentStub, net.minecraft.client.gui.components.events.GuiEventListener {

    @Shadow public boolean active;

    @Shadow protected boolean isHovered;

    @Unique
    protected VanillaWidgetComponent wrapper = null;

    @Override
    public void inflate(Size space) {
        this.owo$getWrapper().inflate(space);
    }

    @Override
    public void mount(ParentUIComponent parent, int x, int y) {
        this.owo$getWrapper().mount(parent, x, y);
    }

    @Override
    public void dismount(DismountReason reason) {
        this.owo$getWrapper().dismount(reason);
    }

    @Nullable
    @Override
    public ParentUIComponent parent() {
        return this.owo$getWrapper().parent();
    }

    @Override
    public @Nullable FocusHandler focusHandler() {
        return this.owo$getWrapper().focusHandler();
    }

    @Override
    public UIComponent positioning(Positioning positioning) {
        this.owo$getWrapper().positioning(positioning);
        return this;
    }

    @Override
    public AnimatableProperty<Positioning> positioning() {
        return this.owo$getWrapper().positioning();
    }

    @Override
    public UIComponent margins(Insets margins) {
        this.owo$getWrapper().margins(margins);
        return this;
    }

    @Override
    public AnimatableProperty<Insets> margins() {
        return this.owo$getWrapper().margins();
    }

    @Override
    public UIComponent horizontalSizing(Sizing horizontalSizing) {
        this.owo$getWrapper().horizontalSizing(horizontalSizing);
        return this;
    }

    @Override
    public UIComponent verticalSizing(Sizing verticalSizing) {
        this.owo$getWrapper().verticalSizing(verticalSizing);
        return this;
    }

    @Override
    public AnimatableProperty<Sizing> horizontalSizing() {
        return this.owo$getWrapper().horizontalSizing();
    }

    @Override
    public AnimatableProperty<Sizing> verticalSizing() {
        return this.owo$getWrapper().verticalSizing();
    }

    @Override
    public EventSource<MouseDown> mouseDown() {
        return this.owo$getWrapper().mouseDown();
    }

    @Override
    public int x() {
        return this.owo$getWrapper().x();
    }

    @Override
    public int y() {
        return this.owo$getWrapper().y();
    }

    @Override
    public int width() {
        return this.owo$getWrapper().width();
    }

    @Override
    public int height() {
        return this.owo$getWrapper().height();
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        this.owo$getWrapper().draw(graphics, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public boolean shouldDrawTooltip(double mouseX, double mouseY) {
        return this.owo$getWrapper().shouldDrawTooltip(mouseX, mouseY);
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        this.owo$getWrapper().update(delta, mouseX, mouseY);
        this.cursorStyle(this.active ? this.owo$preferredCursorStyle() : CursorStyle.POINTER);
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        return this.owo$getWrapper().onMouseDown(click, doubled);
    }

    @Override
    public boolean onMouseUp(MouseButtonEvent click) {
        return this.owo$getWrapper().onMouseUp(click);
    }

    @Override
    public EventSource<MouseUp> mouseUp() {
        return this.owo$getWrapper().mouseUp();
    }

    @Override
    public EventSource<MouseScroll> mouseScroll() {
        return this.owo$getWrapper().mouseScroll();
    }

    @Override
    public EventSource<MouseDrag> mouseDrag() {
        return this.owo$getWrapper().mouseDrag();
    }

    @Override
    public EventSource<KeyPress> keyPress() {
        return this.owo$getWrapper().keyPress();
    }

    @Override
    public EventSource<CharTyped> charTyped() {
        return this.owo$getWrapper().charTyped();
    }

    @Override
    public EventSource<FocusGained> focusGained() {
        return this.owo$getWrapper().focusGained();
    }

    @Override
    public EventSource<FocusLost> focusLost() {
        return this.owo$getWrapper().focusLost();
    }

    @Override
    public EventSource<MouseEnter> mouseEnter() {
        return this.owo$getWrapper().mouseEnter();
    }

    @Override
    public EventSource<MouseLeave> mouseLeave() {
        return this.owo$getWrapper().mouseLeave();
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return this.owo$getWrapper().onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
        return this.owo$getWrapper().onMouseDrag(click, deltaX, deltaY);
    }

    @Override
    public boolean onKeyPress(KeyEvent input) {
        return this.owo$getWrapper().onKeyPress(input);
    }

    @Override
    public boolean onCharTyped(CharacterEvent input) {
        return this.owo$getWrapper().onCharTyped(input);
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    @Override
    public void onFocusGained(FocusSource source) {
        this.setFocused(source == FocusSource.KEYBOARD_CYCLE);
        this.owo$getWrapper().onFocusGained(source);
    }

    @Override
    public void onFocusLost() {
        this.setFocused(false);
        this.owo$getWrapper().onFocusLost();
    }

    @Override
    public <C extends UIComponent> C configure(Consumer<C> closure) {
        return this.owo$getWrapper().configure(closure);
    }

    @Override
    public void parseProperties(UIModel spec, Element element, Map<String, Element> children) {
        // --- copied from Component, because you can't invoke interface super methods in mixins - very cool ---

        if (!element.getAttribute("id").isBlank()) {
            this.id(element.getAttribute("id").strip());
        }

        UIParsing.apply(children, "margins", Insets::parse, this::margins);
        UIParsing.apply(children, "positioning", Positioning::parse, this::positioning);
        UIParsing.apply(children, "cursor-style", UIParsing.parseEnum(CursorStyle.class), this::cursorStyle);
        UIParsing.apply(children, "tooltip-text", UIParsing::parseText, component -> this.tooltip(component));

        if (children.containsKey("sizing")) {
            var sizingValues = UIParsing.childElements(children.get("sizing"));
            UIParsing.apply(sizingValues, "vertical", Sizing::parse, this::verticalSizing);
            UIParsing.apply(sizingValues, "horizontal", Sizing::parse, this::horizontalSizing);
        }

        // --- end ---

        UIParsing.apply(children, "active", UIParsing::parseBool, active -> this.active = active);
    }

    @Override
    public CursorStyle cursorStyle() {
        return this.owo$getWrapper().cursorStyle();
    }

    @Override
    public UIComponent cursorStyle(CursorStyle style) {
        return this.owo$getWrapper().cursorStyle(style);
    }

    @Override
    public UIComponent tooltip(List<ClientTooltipComponent> tooltip) {
        return this.owo$getWrapper().tooltip(tooltip);
    }

    @Override
    public List<ClientTooltipComponent> tooltip() {
        return this.owo$getWrapper().tooltip();
    }

    @Override
    public UIComponent id(@Nullable String id) {
        this.owo$getWrapper().id(id);
        return this;
    }

    @Override
    public @Nullable String id() {
        return this.owo$getWrapper().id();
    }

    @Unique
    protected VanillaWidgetComponent owo$getWrapper() {
        if (this.wrapper == null) {
            this.wrapper = UIComponents.wrapVanillaWidget((AbstractWidget) (Object) this);
        }

        return this.wrapper;
    }

    @Override
    public @Nullable VanillaWidgetComponent widgetWrapper() {
        return this.wrapper;
    }

    @Override
    public int xOffset() {
        return 0;
    }

    @Override
    public int yOffset() {
        return 0;
    }

    @Override
    public int widthOffset() {
        return 0;
    }

    @Override
    public int heightOffset() {
        return 0;
    }

    @Inject(method = "setWidth", at = @At("HEAD"), cancellable = true)
    private void applyWidthToWrapper(int width, CallbackInfo ci) {
        var wrapper = this.wrapper;
        if (wrapper != null) {
            wrapper.horizontalSizing(Sizing.fixed(width));
            ci.cancel();
        }
    }

    @Override
    public void updateX(int x) {
        this.owo$getWrapper().updateX(x);
    }

    @Override
    public void updateY(int y) {
        this.owo$getWrapper().updateY(y);
    }

    protected CursorStyle owo$preferredCursorStyle() {
        return CursorStyle.POINTER;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;renderWidget(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void setHovered(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.wrapper != null) this.isHovered = this.isHovered && this.wrapper.hovered();
    }
}
