package src.game;

import org.lwjgl.opengl.*;
import src.Main;
import src.rendering.Renderer;
import src.rendering.ShaderHelper;
import src.utility.Logging;
import src.utility.MathUtils;
import src.utility.Vec2;

import java.io.File;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    public Window window = new Window();  // handle
    public ShaderHelper sh = new ShaderHelper();

    public Vec2 mousePos = new Vec2();

    public double timeStarted = 0;
    public int secondsElapsed = 0;
    int frameCounter = 0;
    int fps = 0;

    int vertBuff;
    int vao;

    public void start() {
        timeStarted = System.currentTimeMillis();
        Main.startTimeStepper(Constants.DT, this);
    }

    public void createCapabilitiesAndOpen() {
        window.setupContext();
        window.setVSync(Constants.V_SYNC);

        GL.createCapabilities();  // do before anything gl related
        glEnable(GL45.GL_DEBUG_OUTPUT);
        glClearColor(.0f, .0f, .0f, .0f);

        window.show();
        bindEvents();
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
        GL45.glDebugMessageCallback(Logging.debugCallback(), -1);

        // key inputs
        glfwSetKeyCallback(window.handle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        glfwSetCursorPosCallback(window.handle, (window, xpos, ypos) -> mousePos.set(xpos, ypos));
    }

    public void setupBuffers() {
        vao = GL45.glGenVertexArrays();
        GL45.glBindVertexArray(vao);

        // copy vertices into buffer
        vertBuff = GL45.glGenBuffers();
        GL45.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertBuff);
        GL45.glBufferData(GL15.GL_ARRAY_BUFFER, 1024, GL15.GL_DYNAMIC_DRAW);

        // define the format of the buffer
        GL45.glEnableVertexAttribArray(0);  // only need one as we only have vertex position
        GL45.glVertexAttribPointer(0, 2, GL_FLOAT, false, Float.BYTES * 2, 0);
    }

    /** Must be called after window is visible */
    public void setupShaders() {
        sh.genProgram();
        sh.attachShadersInDir(new File(Constants.SHADERS_FOLDER));
        sh.linkProgram();
        GL45.glUseProgram(sh.program);  // bind

        sh.uniform2f("resolution", Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);
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
        Renderer.clearScreen();

        sh.uniform1f("time", (float) glfwGetTime());

        float[] verts = {
                (float) mousePos.x, (float) mousePos.y,
                50, Constants.SCREEN_SIZE.height - 50,
                Constants.SCREEN_SIZE.width - 50, Constants.SCREEN_SIZE.height - 50
        };
        GL45.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, verts);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, (int) Math.floor(verts.length * .5));

        Renderer.finish(window);
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
