package src;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;  // handle
    boolean vSync = false;
    int fps = 0;

    public void run() {
        System.out.println("HARHARHAR");

        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();  // print errors please *-*

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        // Configure window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        // Create the window
        window = glfwCreateWindow(300, 300, "ARGG IT HURTS", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        // events
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        glfwSetWindowPosCallback(window, (window, xpos, ypos) -> {
            System.out.println(xpos + ", " + ypos);
        });

        // Get the thread stack and push a new frame
        if (glfwGetPlatform() != GLFW_PLATFORM_WAYLAND) {  // cause wayland is stupid
            try (MemoryStack stack = stackPush()) {
                IntBuffer winWidth = stack.mallocInt(1);
                IntBuffer winHeight = stack.mallocInt(1);

                glfwGetWindowSize(window, winWidth, winHeight);
                GLFWVidMode screen = glfwGetVideoMode(glfwGetPrimaryMonitor());
                assert screen != null;

                glfwSetWindowPos(
                        window,
                        (screen.width() - winWidth.get(0)) / 2,
                        (screen.height() - winHeight.get(0)) / 2
                );
            }
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(vSync ? 1 : 0);
        glfwShowWindow(window);
    }

    private void loop() {
        // critical for LWJGL's interoperation with GLFW's OpenGL context
        GL.createCapabilities();

        glClearColor(1.0f, 0.0f, 1.0f, 0.0f);

        long lastTime = System.nanoTime();
        int frameCounter = 0;
        float accumulated = 0;

        // rendering loop
        while (!glfwWindowShouldClose(window)) {
            if (accumulated >= 0.5f) {
                fps = frameCounter * 2;
                frameCounter = 0;
                accumulated = 0f;
            }
            long t = System.nanoTime();
            float dt = (t - lastTime) * 1E-9f;
            lastTime = t;
            frameCounter++;
            accumulated += dt;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glfwSwapBuffers(window); // swap the color buffers

            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
