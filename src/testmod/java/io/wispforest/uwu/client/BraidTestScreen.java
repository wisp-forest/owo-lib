package io.wispforest.uwu.client;

import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.Button;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.*;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BraidTestScreen extends Screen {

    private AppState state;

    public BraidTestScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        super.init();

        try {
            if (this.state == null) {
                this.state = new AppState(
                    null,
                    this.client,
                    new TestApp()
                );
            } else {
                this.state.rootInstance().markNeedsLayout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.state == null) return;

        this.state.updateWidgetsAndInteractions(
            mouseX,
            mouseY,
            this.client.getRenderTickCounter().getTickDelta(false),
            this.client.getRenderTickCounter().getLastFrameDuration()
        );

        this.state.draw(context);
    }

    @Override
    public void removed() {
        super.removed();
        if (this.state != null) this.state.dispose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.state.dispatchMouseDownEvent(mouseX, mouseY) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.state.dispatchMouseDragEvent(deltaX, deltaY) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.state.dispatchMouseUpEvent() || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.state.dispatchMouseScrollEvent(mouseX, mouseY, horizontalAmount, verticalAmount) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.state.dispatchKeyDownEvent(keyCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return this.state.dispatchKeyUpEvent(keyCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.state.dispatchCharEvent(chr, modifiers) || super.charTyped(chr, modifiers);
    }

    public static class TestApp extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Center(
                new Column(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new FunnySwitchLayout(),
                    new Padding(Insets.all(10)),
                    new Counter()
                )
            );
        }
    }

    public static class Counter extends StatefulWidget {
        @Override
        public WidgetState<Counter> createState() {
            return new CounterState();
        }
    }

    public static class CounterState extends WidgetState<Counter> {
        private int count = 0;

        @Override
        public Widget build(BuildContext context) {
            return new Sized(
                50.0,
                null,
                new Column(
                    new Label(
                        LabelStyle.DEFAULT,
                        false,
                        Text.literal("count: " + this.count)
                    ),
                    new Row(
                        new Flexible(
                            new Button(
                                Text.literal("+"),
                                () -> this.setState(() -> this.count++)
                            )
                        ),
                        new Flexible(
                            new Button(
                                Text.literal("-"),
                                () -> this.setState(() -> this.count--)
                            )
                        )
                    )
                )
            );
        }
    }

    public static class FunnySwitchLayout extends StatefulWidget {
        @Override
        public WidgetState<FunnySwitchLayout> createState() {
            return new FunnySwitchLayoutState();
        }
    }

    private static class FunnySwitchLayoutState extends WidgetState<FunnySwitchLayout> {
        private LayoutAxis axis = LayoutAxis.HORIZONTAL;

        @Override
        public Widget build(BuildContext context) {
            return new Flex(
                this.axis,
                MainAxisAlignment.START,
                CrossAxisAlignment.CENTER,
                new Button(
                    Text.literal("switch axis"),
                    () -> this.setState(() -> this.axis = this.axis.opposite())
                ),
                new Padding(Insets.all(5)),
                new Panel(
                    OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE,
                    new Padding(
                        Insets.all(10),
                        new Column(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            new Label(Text.literal("that's text")),
                            new Label(Text.literal("some more text")),
                            new Padding(
                                Insets.top(5),
                                new Counter()
                            )
                        )
                    )
                ),
                new Padding(Insets.all(5)),
                new Panel(
                    OwoUIDrawContext.DARK_PANEL_NINE_PATCH_TEXTURE,
                    new Padding(
                        Insets.all(10),
                        new Column(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            new Label(Text.literal("that's text")),
                            new Label(Text.literal("some more text")),
                            new Padding(
                                Insets.top(5),
                                new Counter()
                            )
                        )
                    )
                )
            );
        }
    }
}

