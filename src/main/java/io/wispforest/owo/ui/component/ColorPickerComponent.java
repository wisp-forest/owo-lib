package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.OwoUIPipelines;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.renderstate.GradientQuadElementRenderState;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.w3c.dom.Element;

import java.util.Map;

public class ColorPickerComponent extends BaseUIComponent {

    protected EventStream<OnChanged> changedEvents = OnChanged.newStream();
    protected Observable<Color> selectedColor = Observable.of(Color.BLACK);

    protected @Nullable Section lastClicked = null;

    protected float hue = .5f;
    protected float saturation = 1f;
    protected float value = 1f;
    protected float alpha = 1f;

    protected int selectorWidth = 20;
    protected int selectorPadding = 10;
    protected boolean showAlpha = false;

    // not exactly an ideal solution for location-sensitive cursor
    // styles but the framework doesn't really let us do much
    // better currently
    //
    // glisco, 20.05.2024
    private int lastCursorX;

    public ColorPickerComponent() {
        this.selectedColor.observe(changedEvents.sink()::onChanged);
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        this.lastCursorX = mouseX - this.x;

        // Color area

        graphics.guiRenderState.submitGuiElement(new GradientQuadElementRenderState(
            OwoUIPipelines.GUI_HSV,
            new Matrix3x2f(graphics.pose()),
            new ScreenRectangle(new ScreenPosition(this.renderX(), this.renderY()), this.colorAreaWidth(), this.renderHeight()),
            graphics.scissorStack.peek(),
            new Color(this.hue, 0f, 1f),
            new Color(this.hue, 1f, 1f),
            new Color(this.hue, 0f, 0f),
            new Color(this.hue, 1f, 0f)
        ));

        graphics.drawRectOutline(
                (int) (this.renderX() + (this.saturation * this.colorAreaWidth()) - 1),
                (int) (this.renderY() + ((1 - this.value) * (this.renderHeight() - 1)) - 1),
                3, 3,
                Color.WHITE.argb()
        );

        // Hue selector

        graphics.drawSpectrum(this.renderX() + this.hueSelectorX(), this.renderY(), this.selectorWidth, this.renderHeight(), true);
        graphics.drawRectOutline(
                this.renderX() + this.hueSelectorX() - 1,
                this.renderY() + (int) ((this.renderHeight() - 1) * (1 - this.hue) - 1),
                this.selectorWidth + 2, 3,
                Color.WHITE.argb()
        );

        // Alpha selector

        if (this.showAlpha) {
            var color = 0xFF << 24 | this.selectedColor.get().rgb();
            graphics.drawGradientRect(this.renderX() + this.alphaSelectorX(), this.renderY(), this.selectorWidth, this.renderHeight(), color, color, 0, 0);
            graphics.drawRectOutline(
                    this.renderX() + this.alphaSelectorX() - 1,
                    this.renderY() + (int) ((this.renderHeight() - 1) * (1 - this.alpha) - 1),
                    this.selectorWidth + 2, 3,
                    Color.WHITE.argb()
            );
        }
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        this.lastClicked = this.showAlpha && click.x() >= this.alphaSelectorX()
                ? Section.ALPHA_SELECTOR
                : click.x() > this.hueSelectorX()
                ? Section.HUE_SELECTOR
                : Section.COLOR_AREA;

        this.updateFromMouse(click.x(), click.y());

        super.onMouseDown(click, doubled);
        return true;
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
        this.updateFromMouse(click.x(), click.y());

        super.onMouseDrag(click, deltaX, deltaY);
        return true;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    @Override
    public CursorStyle cursorStyle() {
        var inColorArea = this.lastCursorX >= 0 && this.lastCursorX <= this.colorAreaWidth();
        var inHueSelector = this.lastCursorX >= this.hueSelectorX() && this.lastCursorX <= this.hueSelectorX() + this.selectorWidth;
        var inAlphaSelector = this.showAlpha && this.lastCursorX >= this.alphaSelectorX() && this.lastCursorX <= this.alphaSelectorX() + this.selectorWidth;

        return inColorArea || inHueSelector || inAlphaSelector ? CursorStyle.MOVE : super.cursorStyle();
    }

    protected void updateFromMouse(double mouseX, double mouseY) {
        mouseX = Mth.clamp(mouseX - 1, 0, this.renderWidth());
        mouseY = Mth.clamp(mouseY - 1, 0, this.renderHeight());

        if (this.lastClicked == Section.ALPHA_SELECTOR) {
            this.alpha = 1f - (float) (mouseY / this.renderHeight());
        } else if (this.lastClicked == Section.HUE_SELECTOR) {
            this.hue = 1f - (float) (mouseY / this.renderHeight());
        } else if (this.lastClicked == Section.COLOR_AREA) {
            this.saturation = Math.min(1f, (float) (mouseX / this.colorAreaWidth()));
            this.value = 1f - (float) (mouseY / this.renderHeight());
        }

        this.selectedColor.set(Color.ofHsv(this.hue, this.saturation, this.value, this.alpha));
    }

    protected int renderX() {
        return this.x + 1;
    }

    protected int renderY() {
        return this.y + 1;
    }

    protected int renderWidth() {
        return this.width - 2;
    }

    protected int renderHeight() {
        return this.height - 2;
    }

    protected int colorAreaWidth() {
        return this.showAlpha
                ? this.renderWidth() - this.selectorPadding - this.selectorWidth - this.selectorPadding - this.selectorWidth
                : this.renderWidth() - this.selectorPadding - this.selectorWidth;
    }

    protected int hueSelectorX() {
        return this.showAlpha
                ? this.renderWidth() - this.selectorWidth - this.selectorPadding - this.selectorWidth
                : this.renderWidth() - this.selectorWidth;
    }

    protected int alphaSelectorX() {
        return this.renderWidth() - this.selectorWidth;
    }

    public ColorPickerComponent selectedColor(Color color) {
        this.selectedColor.set(color);

        var hsv = color.hsv();
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
        this.alpha = color.alpha();

        return this;
    }

    public ColorPickerComponent selectedColor(float hue, float saturation, float value) {
        this.selectedColor.set(Color.ofHsv(hue, saturation, value));

        this.hue = hue;
        this.saturation = saturation;
        this.value = value;
        this.alpha = 1;

        return this;
    }

    public Color selectedColor() {
        return this.selectedColor.get();
    }

    public ColorPickerComponent selectorWidth(int selectorWidth) {
        this.selectorWidth = selectorWidth;
        return this;
    }

    public int selectorWidth() {
        return selectorWidth;
    }

    public ColorPickerComponent selectorPadding(int selectorPadding) {
        this.selectorPadding = selectorPadding;
        return this;
    }

    public int selectorPadding() {
        return selectorPadding;
    }

    public ColorPickerComponent showAlpha(boolean showAlpha) {
        this.showAlpha = showAlpha;
        return this;
    }

    public boolean showAlpha() {
        return showAlpha;
    }

    public EventSource<OnChanged> onChanged() {
        return this.changedEvents.source();
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "show-alpha", UIParsing::parseBool, this::showAlpha);
        UIParsing.apply(children, "selector-width", UIParsing::parseUnsignedInt, this::selectorWidth);
        UIParsing.apply(children, "selector-padding", UIParsing::parseUnsignedInt, this::selectorPadding);
        UIParsing.apply(children, "selected-color", Color::parse, this::selectedColor);
    }

    protected enum Section {
        COLOR_AREA, HUE_SELECTOR, ALPHA_SELECTOR
    }

    public interface OnChanged {
        void onChanged(Color color);

        static EventStream<OnChanged> newStream() {
            return new EventStream<>(subscribers -> value -> {
                for (var subscriber : subscribers) {
                    subscriber.onChanged(value);
                }
            });
        }
    }
}
