package boilerplate.common;

import boilerplate.rendering.Renderer;
import org.joml.Vector2f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;
import boilerplate.utility.Logging;

import java.awt.*;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Some simple stuff for window management
 */
public class Window {
    public static class Options {
        public String title = "window title lol";
        public boolean initVisible = false;
        public boolean resizable = false;
        public boolean initCenterWindow = true;  // doesn't work with wayland

        public boolean vSync = false;
        public Dimension initWindowSize = BoilerplateConstants.DEFAULT_SCREEN_SIZE;

        public boolean enableDefaultFramebufferMultisampling = false;
        public int multisamplePerPixel = 4;

        public int glfw_version_major = BoilerplateConstants.GLFW_VERSION_MAJOR;
        public int glfw_version_minor = BoilerplateConstants.GLFW_VERSION_MINOR;
        public int glfw_opengl_profile = BoilerplateConstants.GLFW_OPENGL_PROFILE;
    }

    public long handle;
    private boolean initialised = false;
    private Options options;

    private boolean isWayland;

    public Window() {
        this.options = new Options();
    }

    public Window(Options options) {
        this.options = options;
    }

    public void setOptions(Options options) {
        if (initialised) {
            Logging.danger("Could not set window options as this window has already been setup.");
            return;
        }
        this.options = options;
    }

    /** cause im lazy lol */
    public void quickSetupAndShow(Options options) {
        setOptions(options);
        setup();
        Renderer.setupGLContext(true);
        show();
    }

    public void setup() {
        GLFWErrorCallback.createPrint(System.err).set();  // print errors please *-*

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        isWayland = glfwGetPlatform() == GLFW_PLATFORM_WAYLAND;

        // Configure window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, options.initVisible ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, options.resizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

        // anti aliasing / multi sampling (only on default frame buffer)
        if (options.enableDefaultFramebufferMultisampling) glfwWindowHint(GLFW_SAMPLES, options.multisamplePerPixel);

        // gl version 4.5
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, options.glfw_version_major);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, options.glfw_version_minor);
        glfwWindowHint(GLFW_OPENGL_PROFILE, options.glfw_opengl_profile);

        // Create the window
        handle = glfwCreateWindow(options.initWindowSize.width, options.initWindowSize.height, options.title, NULL, NULL);
        if (handle == NULL) throw new RuntimeException("Failed to create the GLFW window");

        if (options.initCenterWindow) centerWindow();
        setVSync(options.vSync);
        initialised = true;
    }

    public void centerWindow() {
        // center the window (if it can)
        if (!isWaylandPlatform()) {  // cause wayland is stupid
            try (MemoryStack stack = stackPush()) {  // Get the thread stack and push a new frame
                IntBuffer winWidth = stack.mallocInt(1);
                IntBuffer winHeight = stack.mallocInt(1);

                glfwGetWindowSize(handle, winWidth, winHeight);
                GLFWVidMode screen = glfwGetVideoMode(glfwGetPrimaryMonitor());
                if (screen == null) {
                    Logging.danger("An error occurred when attempting to get the screen");
                    return;
                }

                glfwSetWindowPos(
                        handle,
                        (screen.width() - winWidth.get(0)) / 2,
                        (screen.height() - winHeight.get(0)) / 2
                );
            }
        }

        // make context current
        glfwMakeContextCurrent(handle);
        Logging.debug("GLFW context created and current");
    }

    public void show() {
        Logging.info("Opening window:\n--- glfw: '%s'\n--- openGL: '%s'", glfwGetVersionString(), GL45.glGetString(GL45.GL_VERSION));
        glfwShowWindow(handle);
    }

    public void setToClose() {
        glfwSetWindowShouldClose(handle, true);
    }

    public void close() {
        Logging.info("Closing window safely");
        freeCallbacks();
        glfwDestroyWindow(handle);
        glfwTerminate();
        glfwSetErrorCallback(null);
    }

    public void setVSync(boolean vSync) {
        glfwSwapInterval(vSync ? 1 : 0);
    }

    public void setWindowSize(Dimension newSize) {
        glfwSetWindowSize(handle, newSize.width, newSize.height);
    }

    public void setWindowAspectRatio(int numerator, int denominator) {
        glfwSetWindowAspectRatio(handle, numerator, denominator);
    }

    public void unsetWindowAspectRatio() {
        glfwSetWindowAspectRatio(handle, GLFW_DONT_CARE, GLFW_DONT_CARE);
    }

    public void setWindowTitle(String title) {
        glfwSetWindowTitle(handle, title);
    }

    public void setWindowIcon(GLFWImage.Buffer images) {
        glfwSetWindowIcon(handle, images);
    }

    public void setWindowAttrib(int attrib, int value) {
        glfwSetWindowAttrib(handle, attrib, value);
    }

    public boolean isWaylandPlatform() {
        return isWayland;
    }

    public boolean isKeyPressed(int key) {
        return glfwGetKey(handle, key) == GLFW_PRESS;
    }

    public boolean isMouseButtonPressed(int button) {
        return glfwGetMouseButton(handle, button) == GLFW_PRESS;
    }

    public void setCursorPos(Vector2f pos) {
        setCursorPos(pos.x, pos.y);
    }

    public void setCursorPos(float xPos, float yPos) {
        if (isWaylandPlatform()) {
            Logging.warn("Cursor pos cannot be set on Wayland platforms :(");
            return;
        }
        glfwSetCursorPos(handle, xPos, yPos);
    }

    public Vector2f getCursorPos() {
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        glfwGetCursorPos(handle, xPos, yPos);
        return new Vector2f((float) xPos[0], (float) yPos[0]);
    }

    public void hideCursor() {
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
    }

    public void showCursor() {
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void freeCallbacks() {
        glfwFreeCallbacks(handle);
    }
}
