package src.game;

import org.lwjgl.opengl.*;
import src.Main;
import src.utility.Logging;
import src.utility.Vec2;

import java.io.File;
import java.io.PrintStream;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    public Window window = new Window();  // handle
    public int program;
    public boolean optimiseTimeStepper = true;
    boolean vSync = false;  // probably keep off for now

    public double timeStarted = 0;
    public int secondsElapsed = 0;
    int frameCounter = 0;
    int fps = 0;

    int uInxTime;
    int vertBuff;
    int vao;

    public Vec2 mousePos = new Vec2();

    float[] verts = {
        (float) (Constants.SCREEN_SIZE.width * .5), 50,
        50, Constants.SCREEN_SIZE.height - 50,
        Constants.SCREEN_SIZE.width - 50, Constants.SCREEN_SIZE.height - 50
    };

    public void start() {
        timeStarted = System.currentTimeMillis();
        Main.startTimeStepper(Constants.DT, this);
    }

    public void createCapabilitiesAndOpen() {
        window.setupContext();
        window.setVSync(vSync);
        bindEvents();

        GL.createCapabilities();  // do before anything gl related
        GLUtil.setupDebugMessageCallback(new PrintStream(System.err));  // errors please *-*
        glClearColor(.0f, .0f, .0f, .0f);

        window.show();
        setupBuffers();
        setupShaders();
    }

    public void close() {
        Logging.info("Closing safely");
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

        glfwSetCursorPosCallback(window.handle, (window, xpos, ypos) -> mousePos.set(xpos, ypos));
    }

    public void setupBuffers() {
        vao = GL45.glGenVertexArrays();
        GL45.glBindVertexArray(vao);

        // copy vertices into buffer
        vertBuff = GL45.glGenBuffers();
        GL45.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertBuff);
        GL45.glBufferData(GL15.GL_ARRAY_BUFFER, verts, GL15.GL_STATIC_DRAW);

        // define the format of the buffer
        GL45.glEnableVertexAttribArray(0);  // only need one as we only have vertex position
        GL45.glVertexAttribPointer(0, 2, GL_FLOAT, false, Float.BYTES * 2, 0);
    }

    /** Must be called after window is visible */
    public void setupShaders() {
        program = GL45.glCreateProgram();
        File shaderFolder = new File(Constants.SHADERS_FOLDER);
        ShaderHelper.attachShadersInDir(shaderFolder, program);
        ShaderHelper.linkProgram(program);
        GL45.glUseProgram(program);

        // place uniform values
        int resolutionLocation = GL45.glGetUniformLocation(program, "resolution");
        GL45.glUniform2f(resolutionLocation, Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);
        uInxTime = GL45.glGetUniformLocation(program, "time");
    }

    public void updateFps() {
        // update every second
        int newSeconds = (int) Math.floor(MathUtils.millisToSecond(System.currentTimeMillis()) - MathUtils.millisToSecond(timeStarted));
        if (newSeconds != secondsElapsed) {
            fps = frameCounter;
            frameCounter = 0;
            secondsElapsed = newSeconds;
        }
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        // update shader uniforms
        GL45.glUniform1f(uInxTime, (float) glfwGetTime());

        glDrawArrays(GL_TRIANGLE_STRIP, 0, (int) Math.floor(verts.length * .5));

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
