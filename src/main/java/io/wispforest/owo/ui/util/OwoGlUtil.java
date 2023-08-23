package io.wispforest.owo.ui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public final class OwoGlUtil {
    private OwoGlUtil() {

    }

    public static ContextRestorer setContext(long handle) {
        long old = GLFW.glfwGetCurrentContext();
        if (old == handle) return new ContextRestorer(-1);

        GLFW.glfwMakeContextCurrent(handle);

        return new ContextRestorer(old);
    }

    public static ProjectionRestorer setProjectionMatrix(Matrix4f projectionMatrix, VertexSorter sorter) {
        Matrix4f oldMatrix = RenderSystem.getProjectionMatrix();
        VertexSorter oldSorter = RenderSystem.getVertexSorting();

        RenderSystem.setProjectionMatrix(projectionMatrix, sorter);

        return new ProjectionRestorer(oldMatrix, oldSorter);
    }

    public static class ContextRestorer implements AutoCloseable {
        private final long oldContext;

        private ContextRestorer(long old) {
            this.oldContext = old;
        }

        @Override
        public void close() {
            if (oldContext == -1) return;

            GLFW.glfwMakeContextCurrent(oldContext);
        }
    }

    public static class ProjectionRestorer implements AutoCloseable {
        private final Matrix4f matrix;
        private final VertexSorter sorter;

        private ProjectionRestorer(Matrix4f matrix, VertexSorter sorter) {
            this.matrix = matrix;
            this.sorter = sorter;
        }

        @Override
        public void close() {
            RenderSystem.setProjectionMatrix(matrix, sorter);
        }
    }
}
