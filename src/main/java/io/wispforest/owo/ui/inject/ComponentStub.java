package io.wispforest.owo.ui.inject;

import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

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
    default void inflate(Size space) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default void mount(ParentComponent parent, int x, int y) {
        throw new IllegalStateException("Interface stub method called");
    }

    @Override
    default void onDismounted(DismountReason reason) {
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
