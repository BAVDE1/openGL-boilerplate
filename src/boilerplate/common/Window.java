package boilerplate.common;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;
import boilerplate.utility.Logging;

import java.awt.*;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Some simple stuff for window management
 */
public class Window {
    public static class Options {
        public String title = "ARGG IT HURTS";
        public boolean initVisible = false;
        public boolean resizable = false;
        public boolean initCenterWindow = true;

        public boolean vSync = BoilerplateConstants.V_SYNC;
        public Dimension initWindowSize = BoilerplateConstants.SCREEN_SIZE;

        public int glfw_version_major = BoilerplateConstants.GLFW_VERSION_MAJOR;
        public int glfw_version_minor = BoilerplateConstants.GLFW_VERSION_MINOR;
        public int glfw_opengl_profile = BoilerplateConstants.GLFW_OPENGL_PROFILE;
    }

    public long handle;
    private boolean initialised = false;
    private Options options;

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

    public void setup() {
        GLFWErrorCallback.createPrint(System.err).set();  // print errors please *-*

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        // Configure window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, options.initVisible ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, options.resizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

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
        if (glfwGetPlatform() != GLFW_PLATFORM_WAYLAND) {  // cause wayland is stupid
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
        Logging.info("Opening window:\n--- glfw: '%s'\n--- openGL: '%s'", glfwGetVersionString(), GL45.glGetString(GL11.GL_VERSION));
        glfwShowWindow(handle);
    }

    public void close() {
        Logging.info("Closing safely");
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
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
}
