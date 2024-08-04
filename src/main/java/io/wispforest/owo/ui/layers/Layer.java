package io.wispforest.owo.ui.layers;

import io.wispforest.owo.mixin.ui.layers.HandledScreenAccessor;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.util.pond.OwoScreenExtension;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class Layer<S extends Screen, R extends ParentComponent> {

    protected final BiFunction<Sizing, Sizing, R> rootComponentMaker;
    protected final Consumer<Layer<S, R>.Instance> instanceInitializer;

    protected Layer(BiFunction<Sizing, Sizing, R> rootComponentMaker, Consumer<Layer<S, R>.Instance> instanceInitializer) {
        this.rootComponentMaker = rootComponentMaker;
        this.instanceInitializer = instanceInitializer;
    }

    public Instance instantiate(S screen) {
        return new Instance(screen);
    }

    public Instance getInstance(S screen) {
        return ((OwoScreenExtension) screen).owo$getInstance(this);
    }

    public class Instance {

        /**
         * The screen this instance is attached to
         */
        public final S screen;

        /**
         * The UI adapter of this instance - get the {@link OwoUIAdapter#rootComponent}
         * from this to start building your UI tree
         */
        public final OwoUIAdapter<R> adapter;

        /**
         * Whether this layer should aggressively update widget-relative
         * positioning every frame - useful if the targeted widget moves frequently
         */
        public boolean aggressivePositioning = false;

        protected final List<Runnable> layoutUpdaters = new ArrayList<>();

        protected Instance(S screen) {
            this.screen = screen;
            this.adapter = OwoUIAdapter.createWithoutScreen(0, 0, screen.width, screen.height, Layer.this.rootComponentMaker);
            Layer.this.instanceInitializer.accept(this);
        }

        @ApiStatus.Internal
        public void resize(int width, int height) {
            this.adapter.moveAndResize(0, 0, width, height);
        }

        /**
         * Find a widget in the attached screen's widget tree
         *
         * @param locator A predicate to match which identifies the targeted widget
         * @return The targeted widget, or {@link null} if the predicate was never matched
         */
        public @Nullable AbstractWidget queryWidget(Predicate<AbstractWidget> locator) {
            var widgets = new ArrayList<AbstractWidget>();
            for (var element : this.screen.children()) collectChildren(element, widgets);

            AbstractWidget widget = null;
            for (var candidate : widgets) {
                if (!locator.test(candidate)) continue;
                widget = candidate;
                break;
            }

            return widget;
        }

        /**
         * Align the given component to a widget in the attached screen's
         * widget tree. The widget is located by passing the locator predicate to
         * {@link #queryWidget(Predicate)} and getting the position of the resulted widget.
         * <p>
         * If no widget can be found, the component gets positioned at 0,0
         *
         * @param locator       A predicate to match which identifies the targeted widget
         * @param anchor        On which side of the targeted widget to anchor the component
         * @param justification How far along the anchor side of the widget in positive axis direction
         *                      to position the component
         * @param component     The component to position
         */
        public void alignComponentToWidget(Predicate<AbstractWidget> locator, AnchorSide anchor, float justification, Component component) {
            this.layoutUpdaters.add(() -> {
                var widget = this.queryWidget(locator);

                if (widget == null) {
                    component.positioning(Positioning.absolute(0, 0));
                    return;
                }

                var size = component.fullSize();
                switch (anchor) {
                    case TOP -> component.positioning(Positioning.absolute(
                            (int) (widget.getX() + (widget.getWidth() - size.width()) * justification),
                            widget.getY() - size.height()
                    ));
                    case RIGHT -> component.positioning(Positioning.absolute(
                            widget.getX() + widget.getWidth(),
                            (int) (widget.getY() + (widget.getHeight() - size.height()) * justification)
                    ));
                    case BOTTOM -> component.positioning(Positioning.absolute(
                            (int) (widget.getX() + (widget.getWidth() - size.width()) * justification),
                            widget.getY() + widget.getHeight()
                    ));
                    case LEFT -> component.positioning(Positioning.absolute(
                            widget.getX() - size.width(),
                            (int) (widget.getY() + (widget.getHeight() - size.height()) * justification)
                    ));
                }
            });
        }

        /**
         * Align the given component relative to the handled screen coordinates
         * as used by vanilla for positioning slots
         * <p>
         * For obvious reasons, this method may only be invoked on layers which are
         * pushed onto instances of {@link AbstractContainerScreen}
         *
         * @param component The component to position
         * @param x         The X coordinate of the component, relative to the handled screen's origin
         * @param y         The Y coordinate of the component, relative to the handled screen's origin
         */
        public void alignComponentToHandledScreenCoordinates(Component component, int x, int y) {
            if (!(this.screen instanceof AbstractContainerScreen<?> handledScreen)) {
                throw new IllegalStateException("Handled screen coordinates only exist on screens which extend HandledScreen<?>");
            }

            this.layoutUpdaters.add(() -> {
                component.positioning(Positioning.absolute(
                        ((HandledScreenAccessor) handledScreen).owo$getRootX() + x,
                        ((HandledScreenAccessor) handledScreen).owo$getRootY() + y
                ));
            });
        }

        @ApiStatus.Internal
        public void dispatchLayoutUpdates() {
            this.layoutUpdaters.forEach(Runnable::run);
        }

        private static void collectChildren(GuiEventListener element, List<AbstractWidget> children) {
            if (element instanceof AbstractWidget widget) children.add(widget);
            if (element instanceof Layout layout) {
                layout.visitWidgets(child -> collectChildren(child, children));
            }
        }

        public enum AnchorSide {
            TOP, BOTTOM, LEFT, RIGHT
        }
    }

}