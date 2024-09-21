package src.game;

import org.lwjgl.opengl.*;
import src.Main;
import src.rendering.*;
import src.utility.Logging;
import src.utility.MathUtils;
import src.utility.Vec2;

import java.io.File;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    public Window window = new Window();

    ShaderHelper sh = new ShaderHelper();
    VertexArray va = new VertexArray();
    VertexBuffer vb = new VertexBuffer();

    Vec2 mousePos = new Vec2();

    double timeStarted = 0;
    int secondsElapsed = 0;
    int frameCounter = 0;
    int fps = 0;

    public void start() {
        timeStarted = System.currentTimeMillis();
        Main.startTimeStepper(Constants.DT, this);
    }

    public void createCapabilitiesAndOpen() {
        window.setupGLFWContext();
        window.setVSync(Constants.V_SYNC);

        Renderer.setupGLContext();

        window.show();
        bindEvents();
        setupBuffers();
        setupShaders();
    }

    public void close() {
        Logging.info("Closing safely");
        window.close();
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
        Texture t = new Texture("res/textures/explosion.png");

        vb.genId();
        StripBuilder2f s = new StripBuilder2f();
        s.pushSeparatedVertices(new float[] {100, 100, 200, 100, 100, 200});
        s.pushSeparatedVertices(new float[] {300, 100, 400, 100, 300, 200, 400, 200, 300, 300});
        vb.bufferData(s.getSetVertices());

        va.genId();
        VertexArray.VertexArrayLayout layout = new VertexArray.VertexArrayLayout();
        layout.pushFloat(2);
        va.addBuffer(vb, layout);

        TextRenderer r = new TextRenderer();
    }

    /** Must be called after window is visible */
    public void setupShaders() {
        sh.genProgram();
        sh.attachShadersInDir(new File(Constants.SHADERS_FOLDER));
        sh.linkProgram();
        sh.bind();

        sh.uniform2f("resolution", Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);
    }

    public void updateFps() {
        // updates every second
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

        Renderer.draw(GL_TRIANGLE_STRIP, va, 10);

        Renderer.finish(window);
    }

    public double mainLoop(double dt) {
        double tStart = System.nanoTime();
        frameCounter++;

        glfwPollEvents();
        updateFps();
        render();

        return MathUtils.nanoToSecond(System.nanoTime() - tStart);
    }
}
