package io.wispforest.owo.ui.component;

import io.wispforest.owo.shader.HsvProgram;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.w3c.dom.Element;

import java.util.Map;

public class ColorPickerComponent extends BaseComponent {

    protected EventStream<OnChanged> changedEvents = OnChanged.newStream();
    protected Observable<Color> selectedColor = Observable.of(Color.BLACK);

    protected float hue = .5f;
    protected float saturation = 1f;
    protected float value = 1f;
    protected float alpha = 1f;

    protected int selectorWidth = 20;
    protected int selectorPadding = 10;
    protected boolean showAlpha = false;

    public ColorPickerComponent() {
        this.selectedColor.observe(changedEvents.sink()::onChanged);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {

        // Color area

        var buffer = Tessellator.getInstance().getBuffer();
        var matrix = matrices.peek().getPositionMatrix();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, this.renderX(), this.renderY(), 0)
                .color(this.hue, 0f, 1f, 1f).next();
        buffer.vertex(matrix, this.renderX(), this.renderY() + this.renderHeight(), 0)
                .color(this.hue, 0f, 0f, 1f).next();
        buffer.vertex(matrix, this.renderX() + this.colorAreaWidth(), this.renderY() + this.renderHeight(), 0)
                .color(this.hue, 1f, 0f, 1f).next();
        buffer.vertex(matrix, this.renderX() + this.colorAreaWidth(), this.renderY(), 0)
                .color(this.hue, 1f, 1f, 1f).next();

        HsvProgram.INSTANCE.use();
        Tessellator.getInstance().draw();

        Drawer.drawRectOutline(
                matrices,
                (int) (this.renderX() + (this.saturation * this.colorAreaWidth()) - 1),
                (int) (this.renderY() + ((1 - this.value) * (this.renderHeight() - 1)) - 1),
                3, 3,
                Color.WHITE.argb()
        );

        // Hue selector

        Drawer.drawSpectrum(matrices, this.renderX() + this.hueSelectorX(), this.renderY(), this.selectorWidth, this.renderHeight(), true);
        Drawer.drawRectOutline(
                matrices,
                this.renderX() + this.hueSelectorX() - 1,
                this.renderY() + (int) ((this.renderHeight() - 1) * (1 - this.hue) - 1),
                this.selectorWidth + 2, 3,
                Color.WHITE.argb()
        );

        // Alpha selector

        if (this.showAlpha) {
            var color = 0xFF << 24 | this.selectedColor.get().rgb();
            Drawer.drawGradientRect(matrices, this.renderX() + this.alphaSelectorX(), this.renderY(), this.selectorWidth, this.renderHeight(), color, color, 0, 0);
            Drawer.drawRectOutline(
                    matrices,
                    this.renderX() + this.alphaSelectorX() - 1,
                    this.renderY() + (int) ((this.renderHeight() - 1) * (1 - this.alpha) - 1),
                    this.selectorWidth + 2, 3,
                    Color.WHITE.argb()
            );
        }
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        this.updateFromMouse(mouseX, mouseY);

        super.onMouseDown(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        this.updateFromMouse(mouseX, mouseY);

        super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
        return true;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    protected void updateFromMouse(double mouseX, double mouseY) {
        mouseX = MathHelper.clamp(mouseX - 1, 0, this.renderWidth());
        mouseY = MathHelper.clamp(mouseY - 1, 0, this.renderHeight());

        if (this.showAlpha && mouseX >= this.alphaSelectorX()) {
            this.alpha = 1f - (float) (mouseY / this.renderHeight());
        } else if (mouseX >= this.hueSelectorX()) {
            this.hue = 1f - (float) (mouseY / this.renderHeight());
        } else {
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
