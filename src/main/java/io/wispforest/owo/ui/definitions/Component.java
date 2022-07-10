package io.wispforest.owo.ui.definitions;

import io.wispforest.owo.ui.FocusHandler;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.Map;

public interface Component {

    /**
     * Draw the current state of this component onto the screen
     *
     * @param matrices     The transformation stack
     * @param mouseX       The mouse pointer's x-coordinate
     * @param mouseY       The mouse pointer's y-coordinate
     * @param partialTicks The fraction of the current tick that has passed
     * @param delta        The duration of the last frame, in partial ticks
     */
    void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta);

    /**
     * @return The parent of this component
     */
    @Contract(pure = true)
    @Nullable ParentComponent parent();

    /**
     * @return The focus handler of this component hierarchy
     */
    @Contract(pure = true)
    @Nullable FocusHandler focusHandler();

    /**
     * Update this component's positioning and notify the parent
     *
     * @param positioning The new positioning to use
     * @return The component
     */
    Component positioning(Positioning positioning);

    /**
     * @return The positioning of this component
     */
    @Contract(pure = true)
    AnimatableProperty<Positioning> positioning();

    /**
     * Set the external margins of this component and notify the parent
     *
     * @param margins The new margins to use
     */
    Component margins(Insets margins);

    /**
     * @return The external margins of this component
     */
    @Contract(pure = true)
    AnimatableProperty<Insets> margins();

    /**
     * Set the method this component uses to determine its size
     * per axis
     *
     * @param horizontalSizing The new sizing method to use on the x-axis
     * @param verticalSizing   The new sizing method to use on the y-axis
     */
    default Component sizing(Sizing horizontalSizing, Sizing verticalSizing) {
        this.horizontalSizing(horizontalSizing);
        this.verticalSizing(verticalSizing);
        return this;
    }

    /**
     * Set the method this component uses to determine its size
     * on both axes
     *
     * @param sizing The new sizing method to use on both axes
     */
    default Component sizing(Sizing sizing) {
        this.sizing(sizing, sizing);
        return this;
    }

    /**
     * Set the method this component uses to determine its size on the x-axis
     */
    Component horizontalSizing(Sizing horizontalSizing);

    /**
     * @return The sizing method this component uses on the x-axis
     */
    @Contract(pure = true)
    AnimatableProperty<Sizing> horizontalSizing();

    /**
     * Set the method this component uses to determine its size on the y-axis
     */
    Component verticalSizing(Sizing verticalSizing);

    /**
     * @return The sizing method this component uses on the y-axis
     */
    @Contract(pure = true)
    AnimatableProperty<Sizing> verticalSizing();

    /**
     * Set the id of this component. If this is not unique across the hierarchy,
     * calls to {@link ParentComponent#childById(Class, String)} may not be deterministic
     *
     * @param id The new id of this component
     */
    Component id(@Nullable String id);

    /**
     * @return The current id of this component
     */
    @Nullable String id();

    /**
     * Inflate this component into some amount of available space
     *
     * @param space The available space for this component to expand into
     */
    void inflate(Size space);

    /**
     * Called when this component is mounted during the layout process,
     * this must only ever happen after the component has been inflated
     *
     * @param parent The new parent of this component
     * @param x      The new x position of this component
     * @param y      The new y position of this component
     */
    void mount(ParentComponent parent, int x, int y);

    /**
     * Called when this component is being dismounted from its
     * parent. This usually happens because the layout is being recalculated
     * or the child has been removed - useful for releasing resources for example
     *
     * @param reason Why the component is being dismounted. If this is
     *               {@link DismountReason#LAYOUT_INFLATION}, resources should still be held onto
     *               as the component will be re-mounted right after
     */
    void onDismounted(DismountReason reason);

    /**
     * @return {@code true} if this component currently has a parent
     */
    @Contract(pure = true)
    default boolean hasParent() {
        return this.parent() != null;
    }

    /**
     * Called when the mouse has been clicked inside
     * the bounding box of this component
     *
     * @param mouseX The x coordinate at which the mouse was clicked, relative
     *               to this component's bounding box root
     * @param mouseY The y coordinate at which the mouse was clicked, relative
     *               to this component's bounding box root
     * @param button The mouse button which was clicked, refer to the constants
     *               in {@link org.lwjgl.glfw.GLFW}
     * @return {@code true} if this component handled the click and no more
     * components should be notified
     */
    default boolean onMouseClick(double mouseX, double mouseY, int button) {
        return false;
    }

    /**
     * Called when a mouse button has been released
     * while this component is focused
     *
     * @param button The mouse button which was released, refer to the constants
     *               in {@link org.lwjgl.glfw.GLFW}
     * @return {@code true} if this component handled the event and no more
     * components should be notified
     */
    default boolean onMouseRelease(double mouseX, double mouseY, int button) {
        return false;
    }

    /**
     * Called when the mouse has been scrolled inside
     * the bounding box of this component
     *
     * @param mouseX The x coordinate at which the mouse pointer is, relative
     *               to this component's bounding box root
     * @param mouseY The y coordinate at which the mouse pointer is, relative
     *               to this component's bounding box root
     * @param amount How far the mouse was scrolled
     * @return {@code true} if this component handled the scroll event
     * and no more components should be notified
     */
    default boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return false;
    }

    /**
     * Called when the mouse has been dragged
     * while this component is focused
     *
     * @param mouseX The x coordinate at which the mouse was dragged, relative
     *               to this component's bounding box root
     * @param mouseY The y coordinate at which the mouse was dragged, relative
     *               to this component's bounding box root
     * @param deltaX How far the mouse was moved on the x-axis
     * @param deltaY How far the mouse was moved on the y-axis
     * @param button The mouse button which was clicked, refer to the constants
     *               in {@link org.lwjgl.glfw.GLFW}
     * @return {@code true} if this component handled the mouse move and no more
     * components should be notified
     */
    default boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        return false;
    }

    /**
     * Called when a key on the keyboard has been pressed
     * while this component is focused
     *
     * @param keyCode   The key token of the pressed key, refer to the constants in {@link org.lwjgl.glfw.GLFW}
     * @param scanCode  A platform-specific scancode uniquely identifying the exact key that was pressed
     * @param modifiers A bitfield describing which modifier keys were pressed,
     *                  refer to <a href="https://www.glfw.org/docs/3.3/group__mods.html">GLFW Modifier key flags</a>
     * @return {@code true} if this component handled the key-press and no
     * more components should be notified
     */
    default boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    /**
     * Called when a keyboard input event occurred - namely when
     * a key has been pressed and the OS determined it should result
     * in a character being typed
     *
     * @param chr       The character that was typed
     * @param modifiers A bitfield describing which modifier keys were pressed,
     *                  refer to <a href="https://www.glfw.org/docs/3.3/group__mods.html">GLFW Modifier key flags</a>
     * @return {@code true} if this component handled the input and no
     * * more components should be notified
     */
    default boolean onCharTyped(char chr, int modifiers) {
        return false;
    }

    /**
     * @return {@code true} if this component can gain focus
     */
    default boolean canFocus(FocusSource source) {
        return false;
    }

    /**
     * Called when this component gains focus, due
     * to being clicked or selected via tab-cycling
     */
    default void onFocusGained(FocusSource source) {}

    /**
     * Called when this component loses focus
     */
    default void onFocusLost() {}

    /**
     * @return The style of cursor to use while the mouse is
     * hovering this component
     */
    default CursorStyle cursorStyle() {
        return CursorStyle.POINTER;
    }

    /**
     * Update the state of this component
     * before rendering the next frame
     *
     * @param delta The duration of the last frame, in partial ticks
     */
    default void update(float delta) {
        AnimatableProperty.updateAll(delta, this.margins(), this.positioning(), this.horizontalSizing(), this.verticalSizing());
    }

    /**
     * Test whether the given coordinates
     * are inside this component's bounding box
     *
     * @param x The x-coordinate to test
     * @param y The y-coordinate to test
     * @return {@code true} if this component's bounding box encloses
     * the given coordinates
     */
    default boolean isInBoundingBox(double x, double y) {
        return x >= this.x() && x <= this.x() + this.width() && y >= this.y() && y <= this.y() + this.height();
    }

    /**
     * @return The current size of this component's content + its margins
     */
    default Size fullSize() {
        var margins = this.margins().get();
        return Size.of(this.width() + margins.horizontal(), this.height() + margins.vertical());
    }

    /**
     * Read the properties, and potentially children, of this
     * component from the given XML element
     *
     * @param model    The UI model that's being instantiated,
     *                 used for creating child components
     * @param element  The XML element representing this component
     * @param children The child elements of the XML element representing
     *                 this component by tag name, without duplicates
     */
    default void parseProperties(UIModel model, Element element, Map<String, Element> children) {
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
    }

    /**
     * @return The current width of the bounding box
     * of this component
     */
    @Contract(pure = true)
    int width();

    /**
     * @return The current height of the bounding box
     * of this component
     */
    @Contract(pure = true)
    int height();

    /**
     * @return The current x-coordinate of the top-left
     * corner of the bounding box of this component
     */
    @Contract(pure = true)
    int x();

    /**
     * Set the x-coordinate of the top-left corner of the
     * bounding box of this component.
     * <p>
     * This method will usually only be called by the
     * parent component - users of the API
     * should instead alter properties to this component
     * to ensure proper layout updates
     *
     * @param x The new x-coordinate of the top-left corner of the
     *          bounding box of this component
     * @see #positioning(Positioning)
     * @see #margins(Insets)
     */
    void setX(int x);

    /**
     * @return The current y-coordinate of the top-left
     * corner of the bounding box of this component
     */
    @Contract(pure = true)
    int y();

    /**
     * Set the y-coordinate of the top-left corner of the
     * bounding box of this component.
     * <p>
     * This method will usually only be called by the
     * parent component - users of the API
     * should instead alter properties to this component
     * to ensure proper layout updates
     *
     * @param y The new y-coordinate of the top-left corner of the
     *          bounding box of this component
     * @see #positioning(Positioning)
     * @see #margins(Insets)
     */
    void setY(int y);

    /**
     * Set the coordinates of the top-left corner of the
     * bounding box of this component.
     * <p>
     * This method will usually only be called by the
     * parent component - users of the API
     * should instead alter properties to this component
     * to ensure proper layout updates
     *
     * @param y The new coordinates of the top-left corner of the
     *          bounding box of this component
     * @see #positioning(Positioning)
     * @see #margins(Insets)
     */
    default void moveTo(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    enum FocusSource {
        /**
         * The component has been clicked
         */
        MOUSE_CLICK,

        /**
         * The component has been selected by
         * cycling focus via the keyboard
         */
        KEYBOARD_CYCLE
    }

    enum DismountReason {
        /**
         * The child has been dismounted because the parent's layout
         * is being inflated
         */
        LAYOUT_INFLATION,
        /**
         * The child has been dismounted because it has been removed
         * from its parent
         */
        REMOVED
    }
}
