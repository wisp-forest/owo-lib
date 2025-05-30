package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.drag.DragArenaElement;
import io.wispforest.owo.mixin.ui.access.BlockEntityAccessor;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.w3c.dom.Element;

import java.util.function.Consumer;

public class BraidComponent extends BaseComponent {

    private final AppState appState;

    private BraidWidget.State braidWidgetState;

    public BraidComponent(Widget braidWidget) {
        this.appState = new AppState(
            null,
            MinecraftClient.getInstance(),
            new BraidWidget(
                state -> braidWidgetState = state,
                braidWidget
            )
        );
    }

    @Override
    public void inflate(Size space) {
        super.inflate(space);
        braidWidgetState.setState(() -> {
            braidWidgetState.width = this.width;
            braidWidgetState.height = this.height;
        });
    }

    @Override
    public void updateX(int x) {
        super.updateX(x);
        braidWidgetState.setState(() -> braidWidgetState.x = x);
    }

    @Override
    public void updateY(int y) {
        super.updateY(y);
        braidWidgetState.setState(() -> braidWidgetState.y = y);
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        appState.updateWidgetsAndInteractions(
            this.x + mouseX,
            this.y + mouseY,
            MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false),
            delta
        );
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        appState.draw(context);
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        return appState.dispatchMouseDownEvent(mouseX + this.x, mouseY + this.y, button);
    }

    @Override
    public boolean onMouseUp(double mouseX, double mouseY, int button) {
        return appState.dispatchMouseUpEvent(mouseX + this.x, mouseY + this.y, button);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        var x = Screen.hasShiftDown() ? 0 : amount;
        var y = Screen.hasShiftDown() ? amount : 0;
        return appState.dispatchMouseScrollEvent(mouseX + this.x, mouseY + this.y, x, y);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        return appState.dispatchMouseDragEvent(mouseX + this.x, mouseY + this.y, deltaX, deltaY);
    }

    @Override
    public boolean onCharTyped(char chr, int modifiers) {
        return appState.dispatchCharEvent(chr, modifiers);
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        var down = appState.dispatchKeyDownEvent(keyCode, modifiers);
        appState.dispatchKeyUpEvent(keyCode, modifiers);
        return down;
    }

    public static class BraidWidget extends StatefulWidget {

        public final Consumer<State> stateConsumer;

        public final Widget child;

        public BraidWidget(Consumer<State> stateConsumer, Widget child) {
            this.stateConsumer = stateConsumer;
            this.child = child;
        }

        @Override
        public WidgetState<BraidWidget> createState() {
            var state = new State();
            this.stateConsumer.accept(state);
            return state;
        }

        public static class State extends WidgetState<BraidWidget> {
            private int x, y, width, height;

            @Override
            public Widget build(BuildContext context) {
                return new DragArena(
                    new DragArenaElement(
                        x, y,
                        new Sized(
                            width, height,
                            this.widget().child
                        )
                    )
                );
            }
        }
    }
}
