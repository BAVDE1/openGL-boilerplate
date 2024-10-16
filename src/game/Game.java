package src.game;

import org.lwjgl.opengl.GL45;
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

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

public class Game {
    public Window window = new Window();

    ShaderHelper sh = new ShaderHelper();
    VertexArray va = new VertexArray();
    VertexBuffer vb = new VertexBuffer();
    StripBuilder2f sb = new StripBuilder2f();

    TextRenderer.TextObject to1;
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

        FontManager.init();
        FontManager.loadFont(FontManager.FONT_JACQUARD, Font.PLAIN, 42, false);
        FontManager.generateAndBindAllFonts(sh);
    }

    public void close() {
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

            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_W) {
                    to1.setScale(to1.getScale() + .5f);
                }
                if (key == GLFW_KEY_S) {
                    to1.setScale(to1.getScale() - .5f);
                }
            }
        });

        glfwSetCursorPosCallback(window.handle, (window, xPos, yPos) -> mousePos.set(xPos, yPos));
    }

    public void setupBuffers() {
        vb.genId();
        sb.setAdditionalVerts(VertexArray.Layout.defaultLayoutAdditionalVerts());
        sb.pushSeparatedVertices(new float[] {
                200, 300, 0, 1, 1,
                200, 100, 0, 0, 1,
                700, 300, 1, 1, 1,
                700, 100, 1, 0, 1
        });
        sb.pushSeparatedVertices(new float[] {
                50,  400, 0, 1, 2,
                50,  0,   0, 0, 2,
                300, 340, 1, 1, 2,
                300, 50,  1, 0, 2
        });
        sb.pushSeparatedVertices(new float[] {
                750, 350, 0, 1, 1,
                750, 200, 0, 0, 1,
                850, 350, 1, 1, 1,
                850, 200, 1, 0, 1
        });
        vb.bufferData(sb.getSetVertices());

        va.genId();
        va.addBuffer(vb, VertexArray.Layout.getDefaultLayout());

        tr.setupBufferObjects();
        to1 = new TextRenderer.TextObject(1, "N/A", new Vec2f(10, 200));
        tr.pushTextObject(to1);
    }

    /** Must be called after window is visible */
    public void setupShaders() {
        sh.genProgram();
        sh.attachShadersInDir(new File(Constants.SHADERS_FOLDER));
        sh.linkProgram();
        sh.bind();

        sh.uniform2f("resolution", Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);

        new Texture("res/textures/explosion.png").bind(1, sh);
        new Texture("res/textures/closed.png").bind(2, sh);
    }

    public void updateFps() {
        // updates every second
        int newSeconds = (int) Math.floor(MathUtils.millisToSecond(System.currentTimeMillis()) - MathUtils.millisToSecond(timeStarted));
        if (newSeconds != secondsElapsed) {
            fps = frameCounter;
            frameCounter = 0;
            secondsElapsed = newSeconds;
            to1.setString("Secs Elapsed: %s\nFPS: %s", secondsElapsed, fps);
        }
    }

    public void render() {
        Renderer.clearScreen();
        sh.uniform1f("time", (float) glfwGetTime());

        Renderer.draw(GL_TRIANGLE_STRIP, va, sb.vertexCount);
        tr.draw();

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
