package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.ui.inject.ComponentStub;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.w3c.dom.Element;

import java.util.Map;

@SuppressWarnings("ConstantConditions")
@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin implements ComponentStub {

    @Shadow
    protected abstract void setFocused(boolean focused);

    @Shadow
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);

    @Unique
    protected VanillaWidgetComponent owo$wrapper = null;

    @Override
    public void inflate(Size space) {
        this.owo$getWrapper().inflate(space);
    }

    @Override
    public void mount(ParentComponent parent, int x, int y) {
        this.owo$getWrapper().mount(parent, x, y);
    }

    @Override
    public void onDismounted(DismountReason reason) {
        this.owo$getWrapper().onDismounted(reason);
    }

    @Nullable
    @Override
    public ParentComponent parent() {
        return this.owo$getWrapper().parent();
    }

    @Override
    public @Nullable FocusHandler focusHandler() {
        return this.owo$getWrapper().focusHandler();
    }

    @Override
    public Component positioning(Positioning positioning) {
        this.owo$getWrapper().positioning(positioning);
        return this;
    }

    @Override
    public AnimatableProperty<Positioning> positioning() {
        return this.owo$getWrapper().positioning();
    }

    @Override
    public Component margins(Insets margins) {
        this.owo$getWrapper().margins(margins);
        return this;
    }

    @Override
    public AnimatableProperty<Insets> margins() {
        return this.owo$getWrapper().margins();
    }

    @Override
    public Component horizontalSizing(Sizing horizontalSizing) {
        this.owo$getWrapper().horizontalSizing(horizontalSizing);
        return this;
    }

    @Override
    public Component verticalSizing(Sizing verticalSizing) {
        this.owo$getWrapper().verticalSizing(verticalSizing);
        return this;
    }

    @Override
    public AnimatableProperty<Sizing> horizontalSizing() {
        return this.owo$getWrapper().horizontalSizing();
    }

    @Override
    public AnimatableProperty<Sizing> verticalSizing() {
        return this.owo$getWrapper().horizontalSizing();
    }

    @Override
    public int x() {
        return this.owo$getWrapper().x();
    }

    @Override
    public void setX(int x) {
        this.owo$getWrapper().setX(x);
    }

    @Override
    public int y() {
        return this.owo$getWrapper().y();
    }

    @Override
    public void setY(int y) {
        this.owo$getWrapper().setY(y);
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
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        this.owo$getWrapper().draw(matrices, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        return this.mouseClicked(this.x() + mouseX, this.y() + mouseY, button);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return ((ClickableWidget) (Object) this).mouseScrolled(this.x() + mouseX, this.y() + mouseY, amount);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        return ((ClickableWidget) (Object) this).mouseDragged(this.x() + mouseX, this.y() + mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        return ((ClickableWidget) (Object) this).keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onCharTyped(char chr, int modifiers) {
        return ((ClickableWidget) (Object) this).charTyped(chr, modifiers);
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return (Object) this instanceof TextFieldWidget
                || (Object) this instanceof SliderWidget
                || (Object) this instanceof ButtonWidget;
    }

    @Override
    public void onFocusGained(FocusSource source) {
        this.setFocused(source == FocusSource.KEYBOARD_CYCLE);
    }

    @Override
    public void onFocusLost() {
        this.setFocused(false);
    }

    @Override
    public void parseProperties(UIModel spec, Element element, Map<String, Element> children) {
        // --- copied from Component, because you can't invoke interface super methods in mixins - very cool ---

        if (!element.getAttribute("id").isBlank()) {
            this.id(element.getAttribute("id").strip());
        }

        UIParsing.apply(children, "margins", Insets::parse, this::margins);
        UIParsing.apply(children, "positioning", Positioning::parse, this::positioning);

        if (children.containsKey("sizing")) {
            var sizingValues = UIParsing.childElements(children.get("sizing"));
            UIParsing.apply(sizingValues, "vertical", Sizing::parse, this::verticalSizing);
            UIParsing.apply(sizingValues, "horizontal", Sizing::parse, this::horizontalSizing);
        }

        // --- end ---
    }

    @Override
    public Component id(@Nullable String id) {
        this.owo$getWrapper().id(id);
        return this;
    }

    @Override
    public @Nullable String id() {
        return this.owo$getWrapper().id();
    }

    @Unique
    protected VanillaWidgetComponent owo$getWrapper() {
        if (this.owo$wrapper == null) {
            this.owo$wrapper = new VanillaWidgetComponent((ClickableWidget) (Object) this);
        }

        return this.owo$wrapper;
    }
}
