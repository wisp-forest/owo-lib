package io.wispforest.owo.ui.definitions;

import io.wispforest.owo.ui.parsing.IncompatibleUIModelException;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
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
     * @return The children of this component. The iterator
     * of this collection is expected to return the children in reverse
     * insertion order to ensure mouse priority is intuitive
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
    default void update(float delta) {
        Component.super.update(delta);
        AnimatableProperty.updateAll(delta, this.padding());

        for (var child : this.children()) {
            child.update(delta);
        }
    }

    @Override
    default void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        Component.super.parseProperties(model, element, children);
        UIParsing.apply(children, "padding", Insets::parse, this::padding);
        UIParsing.apply(children, "surface", Surface::parse, this::surface);
        UIParsing.apply(children, "vertical-alignment", VerticalAlignment::parse, this::verticalAlignment);
        UIParsing.apply(children, "horizontal-alignment", HorizontalAlignment::parse, this::horizontalAlignment);
        UIParsing.apply(children, "allow-overflow", UIParsing::parseBool, this::allowOverflow);
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
    default <T extends Component> @Nullable T childById(Class<T> expectedClass, String id) {
        for (var child : this.children()) {
            if (Objects.equals(child.id(), id)) {

                if (!expectedClass.isAssignableFrom(child.getClass())) {
                    throw new IncompatibleUIModelException(
                            "Expected child with id '" + id + "'"
                                    + " to be a " + expectedClass.getSimpleName()
                                    + " but it is a " + child.getClass().getSimpleName()
                    );
                }

                return (T) child;
            } else if (child instanceof ParentComponent parent) {
                var candidate = parent.childById(expectedClass, id);
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
