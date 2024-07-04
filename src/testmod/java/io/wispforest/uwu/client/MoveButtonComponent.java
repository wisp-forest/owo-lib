package io.wispforest.uwu.client;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.window.OwoWindow;
import io.wispforest.owo.ui.window.context.CurrentWindowContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class MoveButtonComponent extends ButtonComponent {
    private boolean buttoning = false;

    public MoveButtonComponent(Text message, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) this.buttoning = true;

        return super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseUp(double mouseX, double mouseY, int button) {
        buttoning = false;

        return super.onMouseUp(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        if (buttoning) {
            var window = (OwoWindow<?>) CurrentWindowContext.current();
            int[] windowWidth = new int[1];
            int[] windowHeight = new int[1];

            GLFW.glfwGetWindowSize(window.handle(), windowWidth, windowHeight);

            double factor = window.scaleFactor() * windowWidth[0] / window.framebufferWidth();

            int[] windowX = new int[1];
            int[] windowY = new int[1];

            GLFW.glfwGetWindowPos(window.handle(), windowX, windowY);
            GLFW.glfwSetWindowPos(window.handle(), windowX[0] + (int)(deltaX * factor), windowY[0] + (int)(deltaY * factor));
        }

        return super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
    }
}
