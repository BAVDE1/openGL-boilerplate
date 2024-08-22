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
    boolean vSync = true;
    int fps = 0;

    public void run() {
        System.out.printf("Running '%s'",  glfwGetVersionString());

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
        window = glfwCreateWindow(400, 400, "ARGG IT HURTS", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        // events
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        glfwSetWindowPosCallback(window, (window, xpos, ypos) -> {
            System.out.printf("%s, %s", xpos, ypos);
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
        glClearColor(.0f, .0f, .0f, .0f);

        // SHADER EXAMPLE
        // https://docs.gl/gl4/glAttachShader
//        int program = GL45.glCreateProgram();
//        int shader = GL45.glCreateShader(GL45.GL_FRAGMENT_SHADER);
//        GL45.glShaderSource(shader, chararray);  // load source code of shader as char array
//        GL45.glCompileShader(shader);
//        int[] shaderCompiled = new int[1];
//        GL45.glGetShaderiv(shader, GL45.GL_COMPILE_STATUS, shaderCompiled);
//        if (shaderCompiled[0] != GL_TRUE) {
//            String errorMsg = GL45.glGetShaderInfoLog(shader, 1024);
//        }
//        GL45.glAttachShader(program, shader);
//        GL45.glLinkProgram(program);
//        int[] programLinked = new int[1];
//        GL45.glGetProgramiv(program, GL45.GL_LINK_STATUS, programLinked);
//        if (programLinked[0] != GL_TRUE) {
//            String errorMsg = GL45.glGetProgramInfoLog(shader, 1024);
//        }

        // for 2d like effect
        glOrtho(0, 300, 300, 0, 1, 1);

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

            // render here
            // switch to this https://docs.gl/gl2/glDrawArrays
            glBegin(GL_TRIANGLE_STRIP);  // https://docs.gl/gl2/glBegin
                glColor3f(1, 1, 0);
                glVertex2d(.2, .7);
                glVertex2d(-.5, 0);
                glVertex2d(.5, 0);

                glColor3f(1, 0, 1);
                glVertex2d(-.5, -.2);
                glVertex2d(0, -.4);
                glVertex2d(-.8, -.3);
            glEnd();

            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
