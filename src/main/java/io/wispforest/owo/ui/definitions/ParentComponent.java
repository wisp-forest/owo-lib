package io.wispforest.owo.ui.definitions;

import io.wispforest.owo.ui.parse.OwoUIParsing;
import io.wispforest.owo.ui.parse.OwoUISpec;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public interface ParentComponent extends Component {

    /**
     * Recalculate the layout of this component
     */
    void layout(Size space);

    /**
     * Set how this component should vertically arrange its children
     *
     * @param alignment The new alignment method to use
     */
    ParentComponent verticalAlignment(VerticalAlignment alignment);

    /**
     * @return How this component vertically arranges its children
     */
    VerticalAlignment verticalAlignment();

    /**
     * Set how this component should horizontally arrange its children
     *
     * @param alignment The new alignment method to use
     */
    ParentComponent horizontalAlignment(HorizontalAlignment alignment);

    /**
     * @return How this component horizontally arranges its children
     */
    HorizontalAlignment horizontalAlignment();

    /**
     * Set the internal padding of this component
     *
     * @param padding The new padding to use
     */
    ParentComponent padding(Insets padding);

    /**
     * @return The internal padding of this component
     */
    AnimatableProperty<Insets> padding();

    /**
     * Set if this component should let its children overflow
     * its bounding box
     *
     * @param allowOverflow {@code true} if this component should let
     *                      its children overflow its bounding box
     */
    ParentComponent allowOverflow(boolean allowOverflow);

    /**
     * @return {@code true} if this component allows its
     * children to overflow its bounding box
     */
    boolean allowOverflow();

    /**
     * Set the surface this component uses
     *
     * @param surface The new surface to use
     */
    ParentComponent surface(Surface surface);

    /**
     * @return The surface this component currently uses
     */
    Surface surface();

    /**
     * @return The children of this component
     */
    Collection<Component> children();

    @Override
    default boolean onMouseClick(double mouseX, double mouseY, int button) {
        for (var child : this.children()) {
            if (!child.isInBoundingBox(this.x() + mouseX, this.y() + mouseY)) continue;
            if (child.onMouseClick(this.x() + mouseX - child.x(), this.y() + mouseY - child.y(), button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    default boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        for (var child : this.children()) {
            if (!child.isInBoundingBox(this.x() + mouseX, this.y() + mouseY)) continue;
            if (child.onMouseScroll(this.x() + mouseX - child.x(), this.y() + mouseY - child.y(), amount)) {
                return true;
            }
        }

        return false;
    }

    @Override
    default void updateProperties(float delta) {
        Component.super.updateProperties(delta);
        AnimatableProperty.updateAll(delta, this.padding());

        for (var child : this.children()) {
            child.updateProperties(delta);
        }
    }

    @Override
    default void parseProperties(OwoUISpec spec, Element element, Map<String, Element> children) {
        Component.super.parseProperties(spec, element, children);
        OwoUIParsing.apply(children, "padding", Insets::parse, this::padding);
        OwoUIParsing.apply(children, "surface", Surface::parse, this::surface);
        OwoUIParsing.apply(children, "vertical-alignment", VerticalAlignment::parse, this::verticalAlignment);
        OwoUIParsing.apply(children, "horizontal-alignment", HorizontalAlignment::parse, this::horizontalAlignment);
    }

    /**
     * Recursively find the child with the given id in the
     * hierarchy below this component
     *
     * @param id The id to search for
     * @return The child with the given id, or {@code null} if
     * none was found
     */
    @SuppressWarnings("unchecked")
    default <T extends Component> @Nullable T childById(String id) {
        for (var child : this.children()) {
            if (Objects.equals(child.id(), id)) {
                return (T) child;
            } else if (child instanceof ParentComponent parent) {
                var candidate = parent.<T>childById(id);
                if (candidate != null) return candidate;
            }
        }

        return null;
    }

    /**
     * Called when a child of this parent component has been mutated in some way
     * that would affect the layout of this component
     *
     * @param child The child that has been mutated
     */
    void onChildMutated(Component child);

    /**
     * Get the most specific child at the given coordinates
     *
     * @param x The x-coordinate to query
     * @param y The y-coordinate to query
     * @return The most specific child at the given coordinates,
     * or {@code null} if there is none
     */
    default @Nullable Component childAt(int x, int y) {
        for (var child : this.children()) {
            if (child.isInBoundingBox(x, y)) {
                if (child instanceof ParentComponent parent) {
                    return parent.childAt(x, y);
                } else {
                    return child;
                }
            }
        }

        return this.isInBoundingBox(x, y) ? this : null;
    }

    /**
     * Collect the entire component hierarchy below the given component
     * into the given list
     *
     * @param into The list into which to collect the hierarchy
     */
    default void collectChildren(ArrayList<Component> into) {
        into.add(this);
        for (var child : children()) {
            if (child instanceof ParentComponent parent) {
                parent.collectChildren(into);
            } else {
                into.add(child);
            }
        }
    }
}
