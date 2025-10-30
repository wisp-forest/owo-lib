package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.w3c.dom.Element;

import java.util.Map;

public class SelectableContainer<C extends Component> extends WrappingParentComponent<C> {

    protected Surface highlightSurface = Surface.BLANK;
    protected ValidFocusSource validFocusSource = ValidFocusSource.ANY;

    protected boolean isSelected = false;

    protected SelectableContainer(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(horizontalSizing, verticalSizing, child);
    }

    public void setSelected(boolean value) {
        this.isSelected = value;
    }

    @Override
    protected void updateHoveredState(int mouseX, int mouseY, boolean nowHovered) {
        this.hovered = nowHovered;

        if (nowHovered) {
            this.mouseEnterEvents.sink().onMouseEnter();
        } else {
            this.mouseLeaveEvents.sink().onMouseLeave();
        }
    }

    public SelectableContainer<C> validFocusSource(ValidFocusSource value) {
        this.validFocusSource = value;

        return this;
    }

    public ValidFocusSource validFocusSource() {
        return this.validFocusSource;
    }

    public SelectableContainer<C> highlightSurface(Surface surface) {
        this.highlightSurface = surface;

        return this;
    }

    public Surface highlightSurface() {
        return this.highlightSurface;
    }

    @Override
    public void onFocusGained(FocusSource source) {
        this.isSelected = true;

        super.onFocusGained(source);
    }

    @Override
    public void onFocusLost() {
        this.isSelected = false;

        super.onFocusLost();
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return this.validFocusSource.canFocus(source);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);

        if (this.isSelected || this.hovered) {
            this.highlightSurface.draw(context, this);
        }

        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.children());
    }

    @Override
    public void drawFocusHighlight(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.highlightSurface != Surface.BLANK) {
            this.highlightSurface.draw(context, this);
        } else {
            super.drawFocusHighlight(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "highlight-surface", Surface::parse, this::highlightSurface);
        UIParsing.apply(children, "valid-focus-source", UIParsing.parseEnum(ValidFocusSource.class), this::validFocusSource);
    }

    public enum ValidFocusSource {
        NONE,

        /**
         * The component has been clicked
         */
        MOUSE_CLICK,

        /**
         * The component has been selected by
         * cycling focus via the keyboard
         */
        KEYBOARD_CYCLE,

        /**
         * Any of the above sources are valid to allow focusing
         */
        ANY;

        public boolean canFocus(FocusSource source){
            return switch (this) {
                case MOUSE_CLICK -> source.equals(FocusSource.MOUSE_CLICK);
                case KEYBOARD_CYCLE -> source.equals(FocusSource.KEYBOARD_CYCLE);
                case ANY -> true;
                case NONE -> false;
            };
        }
    }
}
