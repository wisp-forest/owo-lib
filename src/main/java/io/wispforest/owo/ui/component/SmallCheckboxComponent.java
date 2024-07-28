package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.Map;

public class SmallCheckboxComponent extends BaseComponent {

    public static final Identifier TEXTURE = Identifier.of("owo", "textures/gui/smol_checkbox.png");

    protected final EventStream<OnChanged> checkedEvents = OnChanged.newStream();

    protected final Observable<@Nullable Text> label;
    protected boolean labelShadow = false;
    protected boolean checked = false;

    public SmallCheckboxComponent(Text label) {
        this.cursorStyle(CursorStyle.HAND);

        this.label = Observable.of(label);
        this.label.observe(text -> this.notifyParentIfMounted());
    }

    public SmallCheckboxComponent() {
        this(null);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.label.get() != null) {
            context.drawText(MinecraftClient.getInstance().textRenderer, this.label.get(), this.x + 13 + 2, this.y + 3, Color.WHITE.argb(), this.labelShadow);
        }

        context.drawTexture(TEXTURE, this.x, this.y, 13, 13, 0, 0, 13, 13, 32, 16);
        if (this.checked) {
            context.drawTexture(TEXTURE, this.x, this.y, 13, 13, 16, 0, 13, 13, 32, 16);
        }
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.label.get() != null
                ? 13 + 2 + MinecraftClient.getInstance().textRenderer.getWidth(this.label.get())
                : 13;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return 13;
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        boolean result = super.onMouseDown(mouseX, mouseY, button);

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.toggle();
            return true;
        }

        return result;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        boolean result = super.onKeyPress(keyCode, scanCode, modifiers);

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
            this.toggle();
            return true;
        }

        return result;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    public void toggle() {
        this.checked(!this.checked);
        UISounds.playInteractionSound();
    }

    public EventSource<OnChanged> onChanged() {
        return this.checkedEvents.source();
    }

    public SmallCheckboxComponent checked(boolean checked) {
        this.checked = checked;
        this.checkedEvents.sink().onChanged(this.checked);

        return this;
    }

    public boolean checked() {
        return checked;
    }

    public SmallCheckboxComponent label(Text label) {
        this.label.set(label);
        return this;
    }

    public Text label() {
        return this.label.get();
    }

    public SmallCheckboxComponent labelShadow(boolean labelShadow) {
        this.labelShadow = labelShadow;
        return this;
    }

    public boolean labelShadow() {
        return labelShadow;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "label", UIParsing::parseText, this::label);
        UIParsing.apply(children, "label-shadow", UIParsing::parseBool, this::labelShadow);
        UIParsing.apply(children, "checked", UIParsing::parseBool, this::checked);
    }

    public interface OnChanged {
        void onChanged(boolean nowChecked);

        static EventStream<OnChanged> newStream() {
            return new EventStream<>(subscribers -> value -> {
                for (var subscriber : subscribers) {
                    subscriber.onChanged(value);
                }
            });
        }
    }
}
