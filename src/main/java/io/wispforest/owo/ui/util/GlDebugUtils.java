package io.wispforest.owo.ui.util;

import io.wispforest.owo.util.InfallibleCloseable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.KHRDebug;

public final class GlDebugUtils {
    public static final boolean DEBUG_GROUPS_ENABLED = Boolean.getBoolean("owo.glDebugGroups");
    public static final boolean GL_KHR_debug = GL.getCapabilities().GL_KHR_debug;

    private GlDebugUtils() {

    }

    public static void labelObject(int type, int id, String name) {
        if (GL_KHR_debug) {
            KHRDebug.glObjectLabel(type, id, name);
        }
    }

    public static InfallibleCloseable pushGroup(String name) {
        if (!GL_KHR_debug || !DEBUG_GROUPS_ENABLED) return InfallibleCloseable.empty();

        KHRDebug.glPushDebugGroup(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, 42, name);
        return KHRDebug::glPopDebugGroup;
    }
}
