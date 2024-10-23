package src.game;

import org.lwjgl.opengl.GL45;
import src.Main;
import src.rendering.*;
import src.rendering.Shape;
import src.rendering.text.FontManager;
import src.rendering.text.TextRenderer;
import src.utility.Logging;
import src.utility.MathUtils;
import src.utility.Vec2;

import java.awt.*;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

public class Game {
    public static boolean debugMode = false;
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
                switch (key) {
                    case GLFW_KEY_W -> to1.setScale(to1.getScale() + .5f);
                    case GLFW_KEY_S -> to1.setScale(to1.getScale() - .5f);
                    case GLFW_KEY_TAB -> toggleDebug();
                }
            }
        });

        glfwSetCursorPosCallback(window.handle, (window, xPos, yPos) -> mousePos.set((float) xPos, (float) yPos));
    }

    public void setupBuffers() {
        vb.genId();
        va.genId();

        sb.setAdditionalVerts(VertexArray.Layout.defaultLayoutAdditionalVerts());
        sb.pushSeparatedQuad(Shape.createRect(new Vec2(50, 50), new Vec2(500, 100), new Shape.Mode(1, new Vec2(), new Vec2(1))));
        sb.pushSeparatedQuad(Shape.createRect(new Vec2(200, 200), new Vec2(700, 150), new Shape.Mode(2, new Vec2(), new Vec2(1))));
        sb.pushSeparatedQuad(Shape.createLine(new Vec2(70, 20), new Vec2(150, 150), 20, new Shape.Mode(3)));
        sb.pushSeparatedQuad(new Shape.Quad(new Vec2(410, 100), new Vec2(440, 110), new Vec2(460, 180), new Vec2(480, 150), new Shape.Mode(3)));
        Shape.Poly p = Shape.createRectOutline(new Vec2(700, 100), new Vec2(100, 50), 15, new Shape.Mode(3));
        sb.pushSeparatedPolygon(p);

        vb.bufferData(sb.getSetVertices());
        va.addBuffer(vb, VertexArray.Layout.getDefaultLayout());

        tr.setupBufferObjects();
        to1 = new TextRenderer.TextObject(1, "", new Vec2(10, 150));
        tr.pushTextObject(to1);
    }

    /** Must be called after window is visible */
    public void setupShaders() {
        sh.genProgram();
        sh.attachShaders(Constants.SHADER_VERTEX, Constants.SHADER_FRAGMENT);
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
        }
    }

    public void toggleDebug() {
        debugMode = !debugMode;
        sh.uniform1i("debugMode", debugMode ? 1:0);
    }

    public void render() {
        Renderer.clearScreen();
        sh.uniform1f("time", (float) glfwGetTime());

        Renderer.draw(debugMode ? GL_LINE_STRIP : GL_TRIANGLE_STRIP, va, sb.getVertexCount());
        Renderer.draw(tr);

        Renderer.finish(window);
    }

    public double mainLoop(double dt) {
        double tStart = System.nanoTime();
        frameCounter++;

        glfwPollEvents();
        updateFps();
        to1.setString("Secs Elapsed: %s, FPS: %s\nDebug (tab): %s\nv: %s, f: %s, s: %s (%s)", secondsElapsed, fps, debugMode, sb.getVertexCount(), sb.getFloatCount(), sb.getSeparationsCount(), sb.getCurrentFullnessPercent());
        render();

        return MathUtils.nanoToSecond(System.nanoTime() - tStart);
    }
}
