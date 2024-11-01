package src.game;

import org.lwjgl.opengl.GL45;
import src.Main;
import src.rendering.Shape;
import src.rendering.*;
import src.rendering.text.FontManager;
import src.rendering.text.TextRenderer;
import src.utility.Logging;
import src.utility.MathUtils;
import src.utility.Vec2;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Manages everything. Contains the main loop.
 */
public class Game {
    public static boolean debugMode = false;
    public Window window = new Window();

    // main buffers
    ShaderHelper shMain = new ShaderHelper();
    VertexArray vaMain = new VertexArray();
    VertexBuffer vbMain = new VertexBuffer();
    BufferBuilder2f builderMain = new BufferBuilder2f();

    // circle buffers
    ShaderHelper shCircles = new ShaderHelper();
    VertexArray vaCircles = new VertexArray();
    VertexBuffer vbCircles = new VertexBuffer();
    BufferBuilder2f builderCircles = new BufferBuilder2f();

    // text
    TextRenderer.TextObject to1;
    TextRenderer textRenderer = new TextRenderer();

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
        FontManager.loadFont(Font.MONOSPACED, Font.BOLD, 18, true);
        FontManager.generateAndBindAllFonts(shMain);
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
                    case GLFW_KEY_E -> to1.setScale(to1.getScale() + .5f);
                    case GLFW_KEY_Q -> to1.setScale(to1.getScale() - .5f);
                    case GLFW_KEY_TAB -> toggleDebug();
                }
            }
        });

        glfwSetCursorPosCallback(window.handle, (window, xPos, yPos) -> mousePos.set((float) xPos, (float) yPos));
    }

    public void setupBuffers() {
        // MAIN BUFFERS
        vbMain.genId();
        vaMain.genId();

        builderMain.setAdditionalVerts(VertexArray.Layout.defaultLayoutAdditionalVerts());
        builderMain.pushSeparatedQuad(Shape.createRect(new Vec2(50, 50), new Vec2(500, 100), new Shape.Mode(1, new Vec2(), new Vec2(1))));
        builderMain.pushSeparatedQuad(Shape.createRect(new Vec2(200, 200), new Vec2(700, 150), new Shape.Mode(2, new Vec2(), new Vec2(1))));
        builderMain.pushSeparatedQuad(Shape.createLine(new Vec2(70, 20), new Vec2(150, 150), 20, new Shape.Mode(3)));
        builderMain.pushSeparatedQuad(new Shape.Quad(new Vec2(510, 100), new Vec2(540, 110), new Vec2(560, 180), new Vec2(580, 150), new Shape.Mode(3)));
        Shape.Poly p = Shape.createRectOutline(new Vec2(700, 100), new Vec2(100, 50), 15, new Shape.Mode(3));
        builderMain.pushSeparatedPolygon(p);

        Shape.Poly p2 = new Shape.Poly(new Vec2(100, 250), new Shape.Mode(Color.RED), new Vec2(50, 50), new Vec2(-50, 0), new Vec2(50, 0), new Vec2(-50, 50), new Vec2(0, -50));
        Shape.sortPoints(p2);
        builderMain.pushSeparatedPolygonSorted(p2);

        vbMain.bufferData(builderMain.getSetVertices());
        vaMain.pushBuffer(vbMain, VertexArray.Layout.getDefaultLayout());

        textRenderer.setupBufferObjects();
        to1 = new TextRenderer.TextObject(1, "", new Vec2());
        to1.setBgColour(Color.BLACK);
        textRenderer.pushTextObject(to1);

        // CIRCLE BUFFERS
        vbCircles.genId();
        vaCircles.genId();

        builderCircles.setAdditionalVerts(5);
        builderCircles.pushCircle(new Vec2(200), 50, Color.BLUE);
        builderCircles.pushCircle(new Vec2(300), 34, 10, Color.GREEN);

        VertexArray.Layout instanceLayout = new VertexArray.Layout();
        instanceLayout.pushFloat(2);  // circle pos
        instanceLayout.pushFloat(1);  // radius
        instanceLayout.pushFloat(1);  // inner radius
        instanceLayout.pushFloat(3);  // colour
        vbCircles.bufferData(builderCircles.getSetVertices());
        vaCircles.pushBuffer(vbCircles, instanceLayout, 1);

    }

    /** Must be called after window is visible */
    public void setupShaders() {
        shMain.genProgram();
        shMain.attachShaders(Constants.SHADER_VERTEX, Constants.SHADER_FRAGMENT);
        shMain.linkProgram();

        new Texture("res/textures/explosion.png").bind(1, shMain);
        new Texture("res/textures/closed.png").bind(2, shMain);

        shCircles.genProgram();
        shCircles.attachShaders("res/shaders/vs_circle.vert", "res/shaders/fs_circle.frag");
        shCircles.linkProgram();

        ShaderHelper.uniform2f(shMain, "resolution", Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);
        ShaderHelper.uniform2f(shCircles, "resolution", Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);
    }

    public void updateFpsCounterAndDebugText() {
        // updates every second
        int newSeconds = (int) Math.floor(MathUtils.millisToSecond(System.currentTimeMillis()) - MathUtils.millisToSecond(timeStarted));
        if (newSeconds != secondsElapsed) {
            fps = frameCounter;
            frameCounter = 0;
            secondsElapsed = newSeconds;
        }

        // debug string
        to1.setString("Elapsed: %s, FPS: %s\nDebug (tab): %s\ns: %s, v: %s, f: %s/%s (%.5f)",
                secondsElapsed,
                fps,
                debugMode,
                builderMain.getSeparationsCount(),
                builderMain.getVertexCount(),
                builderMain.getFloatCount(),
                builderMain.getBufferSize(),
                builderMain.getCurrentFullnessPercent()
        );
    }

    public void toggleDebug() {
        debugMode = !debugMode;
        ShaderHelper.uniform1i(shMain, "debugMode", debugMode ? 1:0);
        ShaderHelper.uniform1i(shCircles, "debugMode", debugMode ? 1:0);
    }

    public void render() {
        Renderer.clearScreen();
        ShaderHelper.uniform1f(shMain, "time", (float) glfwGetTime());

        Renderer.draw(debugMode ? GL_LINE_STRIP : GL_TRIANGLE_STRIP, vaMain, builderMain.getVertexCount());
        Renderer.draw(textRenderer);

        shCircles.bind();
        Renderer.drawInstanced(GL_TRIANGLES, vaCircles, 3, builderCircles.getVertexCount());

        Renderer.finish(window);
    }

    public double mainLoop(double dt) {
        double tStart = System.nanoTime();
        frameCounter++;

        glfwPollEvents();
        updateFpsCounterAndDebugText();
        render();

        return MathUtils.nanoToSecond(System.nanoTime() - tStart);
    }
}
