package src.game;

import org.lwjgl.opengl.*;
import src.Main;
import src.rendering.*;
import src.rendering.text.FontManager;
import src.rendering.text.TextRenderer;
import src.utility.Logging;
import src.utility.MathUtils;
import src.utility.Vec2;
import src.utility.Vec2f;

import java.awt.*;
import java.io.File;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    public Window window = new Window();

    ShaderHelper sh = new ShaderHelper();
    VertexArray va = new VertexArray();
    VertexBuffer vb = new VertexBuffer();

    TextRenderer tr = new TextRenderer();

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
        setupShaders();
        setupBuffers();
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
        vb.genId();
        StripBuilder2f s = new StripBuilder2f();
        s.setAdditionalVerts(3);
        s.pushSeparatedVertices(new float[] {
                200, 300, 0, 1, 0,
                200, 100, 0, 0, 0,
                700, 300, 1, 1, 0,
                700, 100, 1, 0, 0
        });
        s.pushSeparatedVertices(new float[] {
                50,  200, 0, 1, 1,
                50,  50,  0, 0, 1,
                300, 200, 1, 1, 1,
                300, 50,  1, 0, 1
        });
        vb.bufferData(s.getSetVertices());

        va.genId();
        VertexArray.VertexArrayLayout layout = new VertexArray.VertexArrayLayout();
        layout.pushFloat(2);  // 0: pos
        layout.pushFloat(2);  // 1: tex coord
        layout.pushFloat(1);  // 2: tex slot
        va.addBuffer(vb, layout);

        tr.setupBufferObjects();
        TextRenderer.TextObject to = new TextRenderer.TextObject("a\nstring", new Vec2f(50, 50));
        tr.addTextObject(to);
        tr.buildBuffer();
    }

    /** Must be called after window is visible */
    public void setupShaders() {
        sh.genProgram();
        sh.attachShadersInDir(new File(Constants.SHADERS_FOLDER));
        sh.linkProgram();
        sh.bind();

        sh.uniform2f("resolution", Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);

        new Texture("res/textures/explosion.png").bind(0, sh);
        new Texture("res/textures/closed.png").bind(1, sh);
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

//        tr.draw();
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
