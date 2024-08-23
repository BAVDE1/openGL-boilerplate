package src.game;

import org.lwjgl.opengl.GL;
import src.Main;

import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    public Window window;  // handle
    public boolean optimiseTimeStepper = true;
    boolean vSync = false;  // probably keep off for now

    public double timeStarted = 0;
    public int secondsElapsed = 0;
    int frameCounter = 0;
    int fps = 0;

    public void start() {
        timeStarted = System.currentTimeMillis();
        Main.startTimeStepper(Constants.DT, this);
    }

    public void createCapabilitiesAndOpen() {
        window = new Window();
        window.show();
        window.setVSync(vSync);
        bindEvents();

        GL.createCapabilities();  // critical for LWJGL's interoperation with GLFW's OpenGL context
        glClearColor(.0f, .0f, .0f, .0f);

        // for 2d like effect
        glOrtho(0, 300, 300, 0, 1, 1);
    }

    public void close() {
        System.out.println("Closing safely");
        glfwFreeCallbacks(window.handle);
        glfwDestroyWindow(window.handle);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(window.handle);
    }

    public void bindEvents() {
        // key inputs
        glfwSetKeyCallback(window.handle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);

            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_Q) {
                    this.window.setVSync(vSync = !vSync);
                }
            }
        });

        // window pos change
        glfwSetWindowPosCallback(window.handle, (window, xpos, ypos) -> System.out.printf("%s, %s%n", xpos, ypos));
    }

    public void updateFps() {
        // update every second
        int newSeconds = (int) Math.floor(MathUtils.millisToSecond(System.currentTimeMillis()) - MathUtils.millisToSecond(timeStarted));
        if (newSeconds != secondsElapsed) {
            fps = frameCounter;
            frameCounter = 0;
            secondsElapsed = newSeconds;
            System.out.println(fps);
        }
    }

    public void render() {
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

        glfwSwapBuffers(window.handle); // finish rendering
    }

    public double mainLoop(double dt) {
        double tStart = System.nanoTime();
        frameCounter++;

        updateFps();
        render();
        glfwPollEvents();

        return MathUtils.nanoToSecond(System.nanoTime() - tStart);
    }
}

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
