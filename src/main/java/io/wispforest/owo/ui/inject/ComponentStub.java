package io.wispforest.owo.ui.inject;

import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.event.*;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.util.EventSource;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stub-version of component which adds implementations for all methods
 * which unconditionally throw - used for interface-injecting onto
 * vanilla widgets
 */
@ApiStatus.Internal
public interface ComponentStub extends Component {

    @Override
    default void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default @Nullable ParentComponent parent() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default @Nullable FocusHandler focusHandler() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default Component positioning(Positioning positioning) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default AnimatableProperty<Positioning> positioning() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default Component margins(Insets margins) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default AnimatableProperty<Insets> margins() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default Component horizontalSizing(Sizing horizontalSizing) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default Component verticalSizing(Sizing verticalSizing) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default AnimatableProperty<Sizing> horizontalSizing() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default AnimatableProperty<Sizing> verticalSizing() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default EventSource<MouseEnter> mouseEnter() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default EventSource<MouseLeave> mouseLeave() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default CursorStyle cursorStyle() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default Component cursorStyle(CursorStyle style) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default Component tooltip(List<TooltipComponent> tooltip) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default List<TooltipComponent> tooltip() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default void inflate(Size space) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default void mount(ParentComponent parent, int x, int y) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default void dismount(DismountReason reason) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default int width() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default int height() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default boolean onMouseDown(double mouseX, double mouseY, int button) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default EventSource<MouseDown> mouseDown() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default boolean onMouseUp(double mouseX, double mouseY, int button) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default EventSource<MouseUp> mouseUp() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default EventSource<MouseScroll> mouseScroll() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default EventSource<MouseDrag> mouseDrag() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default EventSource<KeyPress> keyPress() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default boolean onCharTyped(char chr, int modifiers) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default EventSource<CharTyped> charTyped() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default void onFocusGained(FocusSource source) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default EventSource<FocusGained> focusGained() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default void onFocusLost() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default EventSource<FocusLost> focusLost() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default int x() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default void setX(int x) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default int y() {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default void setY(int y) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default Component id(@Nullable String id) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default @Nullable String id() {
        throw new IllegalStateException("Interface stub method called");
    }
}
