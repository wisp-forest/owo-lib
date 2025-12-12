package io.wispforest.owo.braid.display;

import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.owo.braid.core.events.MouseMoveEvent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2dc;

import java.util.ArrayList;
import java.util.List;

public class BraidDisplayBinding {

    private static final List<BraidDisplay> ACTIVE_DISPLAYS = new ArrayList<>();

    // ---

    public static void activate(BraidDisplay display) {
        ACTIVE_DISPLAYS.add(display);
    }

    public static void deactivate(BraidDisplay display) {
        ACTIVE_DISPLAYS.remove(display);
    }

    // ---

    public static @Nullable DisplayHitResult targetDisplay;

    @ApiStatus.Internal
    public static @Nullable DisplayHitResult queryTargetDisplay(Vec3 rayOrigin, Vec3 rayDirection) {
        DisplayHitResult closestResult = null;
        double closestRayOffset = Double.POSITIVE_INFINITY;

        for (var display : ACTIVE_DISPLAYS) {
            var result = display.quad.hitTest(rayOrigin, rayDirection);
            if (result == null || result.t() >= closestRayOffset) continue;

            closestResult = new DisplayHitResult(display, result.point());
            closestRayOffset = result.t();
        }

        return closestResult;
    }

    @ApiStatus.Internal
    public static void onDisplayHit(DisplayHitResult targetDisplay) {
        var app = targetDisplay.display.app;

        var cursorX = targetDisplay.point.x() * app.surface.width();
        var cursorY = targetDisplay.point.y() * app.surface.height();

        app.eventBinding.add(new MouseMoveEvent(cursorX, cursorY));
    }

    @ApiStatus.Internal
    public static void updateAndDrawDisplays() {
        for (var display : ACTIVE_DISPLAYS) {
            display.updateAndDrawApp();
        }
    }

    @ApiStatus.Internal
    public static void renderAutomaticDisplays(PoseStack matrices, CameraRenderState camera, SubmitNodeCollector nodeCollector) {
        for (var display : ACTIVE_DISPLAYS) {
            if (!display.renderAutomatically) continue;

            matrices.pushPose();
            matrices.translate(display.quad.pos.subtract(camera.pos));

            display.render(matrices, nodeCollector, LightTexture.FULL_BRIGHT);

            matrices.popPose();
        }
    }

    // ---

    public record DisplayHitResult(BraidDisplay display, Vector2dc point) {}
}
