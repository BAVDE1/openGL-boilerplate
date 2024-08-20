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
        glfwWindowHint(GLFW_POSITION_X, 500);
        glfwWindowHint(GLFW_POSITION_Y, 500);

        // Create the window
        window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
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
//        try (MemoryStack stack = stackPush()) {
//            IntBuffer pWidth = stack.mallocInt(1);
//            IntBuffer pHeight = stack.mallocInt(1);
//
//            glfwGetWindowSize(window, pWidth, pHeight);
//            GLFWVidMode screen = glfwGetVideoMode(glfwGetPrimaryMonitor());
//            assert screen != null;
//
//            System.out.println(GLFW_PLATFORM_WAYLAND == glfwGetPlatform());
//
////            glfwSetWindowPos(
////                    window,
////                    (screen.width() - pWidth.get(0)) / 2,
////                    (screen.height() - pHeight.get(0)) / 2
////            );
//        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(vSync ? 1 : 0);

        glfwShowWindow(window);
    }

    private void loop() {
        // critical for LWJGL's interoperation with GLFW's OpenGL context
        GL.createCapabilities();

        glClearColor(1.0f, 0.0f, 1.0f, 0.0f);

        long lastTime = System.nanoTime();
        int fCounter = 0;
        float frameTime = 0;

        // rendering loop
        while (!glfwWindowShouldClose(window)) {
            if (frameTime >= 0.5f) {
                System.out.println(fCounter * 2);
                fCounter = 0;
                frameTime = 0f;
            }
            long thisTime = System.nanoTime();
            float dt = (thisTime - lastTime) * 1E-9f;
            lastTime = thisTime;
            fCounter++;
            frameTime += dt;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glfwSwapBuffers(window); // swap the color buffers

            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
