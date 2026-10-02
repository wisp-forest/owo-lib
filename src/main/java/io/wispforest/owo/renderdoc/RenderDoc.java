package io.wispforest.owo.renderdoc;

import com.mojang.blaze3d.platform.InputConstants;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import io.wispforest.owo.Owo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.linux.DynamicLinkLoader;
import org.lwjgl.sdl.SDLScancode;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

@ApiStatus.Experimental
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class RenderDoc {

    private RenderDoc() {}

    private static final RenderdocLibrary.RenderdocApi renderdoc;

    static {
        var apiPointer = new PointerByReference();
        RenderdocLibrary.RenderdocApi apiInstance = null;

        var os = Util.getPlatform();

        if (os == Util.OS.WINDOWS || os == Util.OS.LINUX) {
            try {
                RenderdocLibrary renderdocLibrary;
                if (os == Util.OS.WINDOWS) {
                    renderdocLibrary = Native.load("renderdoc", RenderdocLibrary.class);
                } else {
                    int flags = DynamicLinkLoader.RTLD_NOW | DynamicLinkLoader.RTLD_NOLOAD;
                    if (DynamicLinkLoader.dlopen("librenderdoc.so", flags) == 0) {
                        throw new UnsatisfiedLinkError();
                    }

                    renderdocLibrary = Native.load("renderdoc", RenderdocLibrary.class, Map.of(Library.OPTION_OPEN_FLAGS, flags));
                }

                int initResult = renderdocLibrary.RENDERDOC_GetAPI(10500, apiPointer);
                if (initResult != 1) {
                    Owo.LOGGER.error("Could not connect to RenderDoc API, return code: {}", initResult);
                } else {
                    apiInstance = new RenderdocLibrary.RenderdocApi(apiPointer.getValue());

                    var major = new IntByReference();
                    var minor = new IntByReference();
                    var patch = new IntByReference();
                    apiInstance.GetAPIVersion.call(major, minor, patch);
                    Owo.LOGGER.info("Connected to RenderDoc API v" + major.getValue() + "." + minor.getValue() + "." + patch.getValue());
                }
            } catch (UnsatisfiedLinkError ignored) {}
        }

        renderdoc = apiInstance;
    }

    /**
     * @return {@code true} if the RenderDoc dynamic library is loaded
     * and owo has successfully connected to the API
     */
    public static boolean isAvailable() {
        return renderdoc != null;
    }

    /**
     * @return The version of the RenderDoc API that owo is connected to,
     * in &lt;major&gt;.&lt;minor&gt;.&lt;patch&gt; semver format
     */
    public static String getAPIVersion() {
        if (renderdoc == null) return "not connected";

        var major = new IntByReference();
        var minor = new IntByReference();
        var patch = new IntByReference();
        renderdoc.GetAPIVersion.call(major, minor, patch);

        return major.getValue() + "." + minor.getValue() + "." + patch.getValue();
    }

    /**
     * Set the value of a RenderDoc capture option
     *
     * @param option The option to modify
     * @param value  The value to change the option to
     * @return {@code true} if the value was correct and the option
     * was successfully modified
     */
    public static <T> boolean setCaptureOption(CaptureOption<T> option, T value) {
        if (renderdoc == null) return false;

        if (value instanceof Boolean bool) {
            return renderdoc.SetCaptureOptionU32.call(option.idx, new RenderdocLibrary.uint32_t(bool ? 1 : 0)) == 1;
        } else if (value instanceof Integer uint) {
            return renderdoc.SetCaptureOptionU32.call(option.idx, new RenderdocLibrary.uint32_t(uint)) == 1;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Get the value of a RenderDoc capture option
     *
     * @param option The option to query
     * @return The current value of the option
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCaptureOption(CaptureOption<T> option) {
        if (renderdoc == null) return null;

        if (option.type == Boolean.class) {
            return (T) Boolean.valueOf(renderdoc.GetCaptureOptionU32.call(option.idx).intValue() == 1);
        } else if (option.type == Integer.class) {
            return (T) Integer.valueOf(renderdoc.GetCaptureOptionU32.call(option.idx).intValue());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Set the hotkeys used to trigger a capture
     */
    public static void setCaptureKeys(Key... keys) {
        if (renderdoc == null) return;
        renderdoc.SetCaptureKeys.call(Arrays.stream(keys).mapToInt(value -> value.keycode).toArray(), keys.length);
    }

    /**
     * Query the current configuration of the RenderDoc overlay
     *
     * @return All parts of the overlay which are currently enabled
     */
    public static EnumSet<OverlayOption> getOverlayOptions() {
        if (renderdoc == null) return null;

        int mask = renderdoc.GetOverlayBits.call().intValue();

        var set = EnumSet.noneOf(OverlayOption.class);
        for (var option : OverlayOption.values()) {
            if ((mask & option.mask) != 0) set.add(option);
        }

        return set;
    }

    /**
     * Enable some parts of the RenderDoc overlay
     *
     * @param options The options to enable
     */
    public static void enableOverlayOptions(OverlayOption... options) {
        if (renderdoc == null) return;

        int mask = 0;
        for (var option : options) mask |= option.mask;

        renderdoc.MaskOverlayBits.call(new RenderdocLibrary.uint32_t(~0), new RenderdocLibrary.uint32_t(mask));
    }

    /**
     * Disable some parts of the RenderDoc overlay
     *
     * @param options The options to enable
     */
    public static void disableOverlayOptions(OverlayOption... options) {
        if (renderdoc == null) return;

        int mask = 0;
        for (var option : options) mask |= option.mask;

        renderdoc.MaskOverlayBits.call(new RenderdocLibrary.uint32_t(~mask), new RenderdocLibrary.uint32_t(0));
    }

    /**
     * Try to remove all RenderDoc hooks from the process. If this
     * is called after a graphics API has been initialized, behavior
     * is undefined
     */
    public static void removeHooks() {
        if (renderdoc == null) return;
        renderdoc.RemoveHooks.call();
    }

    /**
     * Remove RenderDoc's crash handler from the process
     */
    public static void unloadCrashHandler() {
        if (renderdoc == null) return;
        renderdoc.UnloadCrashHandler.call();
    }

    /**
     * Set the template used to generate new capture file names
     */
    public static void setCaptureFilePathTemplate(String template) {
        if (renderdoc == null) return;
        renderdoc.SetCaptureFilePathTemplate.call(template);
    }

    /**
     * @return the template used to generate new capture file names
     */
    public static String getCaptureFilePathTemplate() {
        if (renderdoc == null) return null;
        return renderdoc.GetCaptureFilePathTemplate.call();
    }

    /**
     * Query information about a specific capture
     *
     * @param index The index to query
     * @return The path and timestamp of the capture at the given index,
     * or {@code null} if no such capture exists
     */
    public static Capture getCapture(int index) {
        if (renderdoc == null) return null;

        var length = new IntByReference();
        if (renderdoc.GetCapture.call(index, null, length, null).intValue() != 1) {
            return null;
        }

        var filename = new byte[length.getValue()];
        var timestamp = new LongByReference();

        renderdoc.GetCapture.call(index, filename, length, timestamp);
        return new Capture(new String(filename, 0, filename.length - 1), Instant.ofEpochSecond(timestamp.getValue()));
    }

    /**
     * @return How many captures have been made
     */
    public static int getNumCaptures() {
        if (renderdoc == null) return -1;
        return renderdoc.GetNumCaptures.call().intValue();
    }

    /**
     * Trigger a capture of the next frame, as
     * if the user had pressed on the capture hotkeys
     */
    public static void triggerCapture() {
        if (renderdoc == null) return;
        renderdoc.TriggerCapture.call();
    }

    /**
     * Immediately begin a capture
     */
    public static void startFrameCapture() {
        if (renderdoc == null) return;
        renderdoc.StartFrameCapture.call(null, null);
    }

    /**
     * @return {@code true} if a capture is currently being performed
     */
    public static boolean isFrameCapturing() {
        if (renderdoc == null) return false;
        return renderdoc.IsFrameCapturing.call().intValue() == 1;
    }

    /**
     * Immediately end an active capture
     */
    public static void endFrameCapture() {
        if (renderdoc == null) return;
        renderdoc.EndFrameCapture.call(null, null);
    }

    /**
     * @return {@code true} if a RenderDoc replay UI
     * instance is currently attached to this process
     */
    public static boolean isReplayUIConnected() {
        if (renderdoc == null) return false;
        return renderdoc.IsTargetControlConnected.call().intValue() == 1;
    }

    /**
     * Open the RenderDoc replay UI
     *
     * @param connect {@code true} if the new UI instance should instantly
     *                attach to this process
     * @return The PID of the spawned process, or {@code 0} if the UI could not be opened
     */
    public static int launchReplayUI(boolean connect) {
        if (renderdoc == null) return -1;
        return renderdoc.LaunchReplayUI.call(new RenderdocLibrary.uint32_t(connect ? 1 : 0), null).intValue();
    }

    /**
     * Request the currently connected replay UI to raise
     * its window to the top - this is not guaranteed to work on every OS
     *
     * @return {@code true} if the UI tried to raise its window, {@code false}
     * if some error occurred while passing on the command or no UI is connected
     */
    public static boolean showReplayUI() {
        if (renderdoc == null) return false;
        return renderdoc.ShowReplayUI.call().intValue() == 1;
    }

    /**
     * Set the comments attached to a specific capture
     *
     * @param capture  The capture to modify, obtain with {@link #getCapture(int)}
     * @param comments The new capture comments
     */
    public static void setCaptureComments(Capture capture, String comments) {
        if (renderdoc == null) return;
        renderdoc.SetCaptureFileComments.call(capture.path, comments);
    }

    public static final class CaptureOption<T> {
        public static final CaptureOption<Boolean> ALLOW_VSYNC = new CaptureOption<>(0, Boolean.class);
        public static final CaptureOption<Boolean> ALLOW_FULLSCREEN = new CaptureOption<>(1, Boolean.class);
        public static final CaptureOption<Boolean> API_VALIDATION = new CaptureOption<>(2, Boolean.class);
        public static final CaptureOption<Boolean> CAPTURE_CALLSTACKS = new CaptureOption<>(3, Boolean.class);
        public static final CaptureOption<Boolean> CAPTURE_CALLSTACKS_ONLY_DRAWS = new CaptureOption<>(4, Boolean.class);
        public static final CaptureOption<Integer> DELAY_FOR_DEBUGGER = new CaptureOption<>(5, Integer.class);
        public static final CaptureOption<Boolean> VERIFY_BUFFER_ACCESS = new CaptureOption<>(6, Boolean.class);
        public static final CaptureOption<Boolean> HOOK_INTO_CHILDREN = new CaptureOption<>(7, Boolean.class);
        public static final CaptureOption<Boolean> REF_ALL_RESOURCES = new CaptureOption<>(8, Boolean.class);
        public static final CaptureOption<Boolean> SAVE_ALL_INITIALS = new CaptureOption<>(9, Boolean.class);
        public static final CaptureOption<Boolean> CAPTURE_ALL_CMD_LISTS = new CaptureOption<>(10, Boolean.class);
        public static final CaptureOption<Boolean> DEBUG_OUTPUT_MUTE = new CaptureOption<>(11, Boolean.class);

        @Deprecated
        public static final CaptureOption<?> ALLOW_UNSUPPORTED_VENDOR_EXTENSIONS = new CaptureOption<>(12, Void.class);

        public final int idx;
        private final Class<T> type;

        CaptureOption(int idx, Class<T> type) {
            this.idx = idx;
            this.type = type;
        }
    }

    public enum Key {
        // '0' - '9' matches ASCII values
        ZERO(0x30, InputConstants.KEY_0),
        ONE(0x31, InputConstants.KEY_1),
        TWO(0x32, InputConstants.KEY_2),
        THREE(0x33, InputConstants.KEY_2),
        FOUR(0x34, InputConstants.KEY_4),
        FIVE(0x35, InputConstants.KEY_5),
        SIX(0x36, InputConstants.KEY_6),
        SEVEN(0x37, InputConstants.KEY_7),
        EIGHT(0x38, InputConstants.KEY_8),
        NINE(0x39, InputConstants.KEY_9),

        // 'A' - 'Z' matches ASCII values
        A(0x41, InputConstants.KEY_A),
        B(0x42, InputConstants.KEY_B),
        C(0x43, InputConstants.KEY_C),
        D(0x44, InputConstants.KEY_D),
        E(0x45, InputConstants.KEY_E),
        F(0x46, InputConstants.KEY_F),
        G(0x47, InputConstants.KEY_G),
        H(0x48, InputConstants.KEY_H),
        I(0x49, InputConstants.KEY_I),
        J(0x4A, InputConstants.KEY_J),
        K(0x4B, InputConstants.KEY_K),
        L(0x4C, InputConstants.KEY_L),
        M(0x4D, InputConstants.KEY_M),
        N(0x4E, InputConstants.KEY_N),
        O(0x4F, InputConstants.KEY_O),
        P(0x50, InputConstants.KEY_P),
        Q(0x51, InputConstants.KEY_Q),
        R(0x52, InputConstants.KEY_R),
        S(0x53, InputConstants.KEY_S),
        T(0x54, InputConstants.KEY_T),
        U(0x55, InputConstants.KEY_U),
        V(0x56, InputConstants.KEY_V),
        W(0x57, InputConstants.KEY_W),
        X(0x58, InputConstants.KEY_X),
        Y(0x59, InputConstants.KEY_Y),
        Z(0x5A, InputConstants.KEY_Z),

        // leave the rest of the ASCII range free
        // in case we want to use it later
        NON_PRINTABLE(0x100, -1),

        DIVIDE(0x101, SDLScancode.SDL_SCANCODE_KP_DIVIDE),
        MULTIPLY(0x102, InputConstants.KEY_MULTIPLY),
        SUBTRACT(0x103, SDLScancode.SDL_SCANCODE_KP_MINUS),
        PLUS(0x104, InputConstants.KEY_ADD),

        F1(0x105, InputConstants.KEY_F1),
        F2(0x106, InputConstants.KEY_F2),
        F3(0x107, InputConstants.KEY_F3),
        F4(0x108, InputConstants.KEY_F4),
        F5(0x109, InputConstants.KEY_F5),
        F6(0x10a, InputConstants.KEY_F6),
        F7(0x10b, InputConstants.KEY_F7),
        F8(0x10c, InputConstants.KEY_F8),
        F9(0x10d, InputConstants.KEY_F9),
        F10(0x10e, InputConstants.KEY_F10),
        F11(0x10f, InputConstants.KEY_F11),
        F12(0x110, InputConstants.KEY_F12),

        HOME(0x111, InputConstants.KEY_HOME),
        END(0x112, InputConstants.KEY_END),
        INSERT(0x113, InputConstants.KEY_INSERT),
        DELETE(0x114, InputConstants.KEY_DELETE),
        PAGE_UP(0x115, InputConstants.KEY_PAGEUP),
        PAGE_DOWN(0x116, InputConstants.KEY_PAGEDOWN),

        BACKSPACE(0x117, InputConstants.KEY_BACKSPACE),
        TAB(0x118, InputConstants.KEY_TAB),
        PRINT_SCREEN(0x119, InputConstants.KEY_PRINTSCREEN),
        PAUSE(0x11a, InputConstants.KEY_PAUSE);

        private final int keycode;
        private final int glfw;

        Key(int keycode, int glfw) {
            this.keycode = keycode;
            this.glfw = glfw;
        }

        private static final Int2ObjectMap<Key> GLFW_MAPPINGS = new Int2ObjectOpenHashMap<>();

        public static @Nullable Key fromGLFW(int glfw) {
            return GLFW_MAPPINGS.getOrDefault(glfw, null);
        }

        static {
            for (var key : values()) {
                if (key.glfw < 0) continue;
                GLFW_MAPPINGS.put(key.glfw, key);
            }
        }
    }

    public enum OverlayOption {
        ENABLED(0x1),
        FRAME_RATE(0x2),
        FRAME_NUMBER(0x4),
        CAPTURE_LIST(0x8),
        DEFAULT(ENABLED.mask | FRAME_RATE.mask | FRAME_NUMBER.mask | CAPTURE_LIST.mask),
        ALL(~0),
        NONE(0);

        public final int mask;

        OverlayOption(int mask) {
            this.mask = mask;
        }
    }

    public record Capture(String path, Instant timestamp) {}
}
