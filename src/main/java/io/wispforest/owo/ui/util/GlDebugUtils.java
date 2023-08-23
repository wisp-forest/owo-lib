package io.wispforest.owo.ui.util;


import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.KHRDebug;

public final class GlDebugUtils {
    public static final boolean GL_KHR_debug = GL.getCapabilities().GL_KHR_debug;

    private GlDebugUtils() {

    }

    public static void labelObject(int type, int id, String name) {
        if (GL_KHR_debug) {
            KHRDebug.glObjectLabel(type, id, name);
        }
    }

    public static DebugGroup pushGroup(String name) {
        if (GL_KHR_debug) {
            KHRDebug.glPushDebugGroup(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, 42, name);
        }

        return new DebugGroup();
    }

    public static class DebugGroup implements AutoCloseable {
        private DebugGroup() {

        }

        @Override
        public void close() {
            if (GL_KHR_debug) {
                KHRDebug.glPopDebugGroup();
            }
        }
    }
}
