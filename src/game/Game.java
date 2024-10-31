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

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    public static boolean debugMode = false;
    public Window window = new Window();

    // main buffers
    ShaderHelper sh_main = new ShaderHelper();
    VertexArray va_main = new VertexArray();
    VertexBuffer vb_main = new VertexBuffer();
    StripBuilder2f sb_main = new StripBuilder2f();

    // circle buffers
    ShaderHelper sh_circles = new ShaderHelper();
    VertexArray va_circles = new VertexArray();
    VertexBuffer vb_circles = new VertexBuffer();
    StripBuilder2f sb_circles = new StripBuilder2f();

    // other
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
        FontManager.generateAndBindAllFonts(sh_main);
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
        vb_main.genId();
        va_main.genId();

        sb_main.setAdditionalVerts(VertexArray.Layout.defaultLayoutAdditionalVerts());
        sb_main.pushSeparatedQuad(Shape.createRect(new Vec2(50, 50), new Vec2(500, 100), new Shape.Mode(1, new Vec2(), new Vec2(1))));
        sb_main.pushSeparatedQuad(Shape.createRect(new Vec2(200, 200), new Vec2(700, 150), new Shape.Mode(2, new Vec2(), new Vec2(1))));
        sb_main.pushSeparatedQuad(Shape.createLine(new Vec2(70, 20), new Vec2(150, 150), 20, new Shape.Mode(3)));
        sb_main.pushSeparatedQuad(new Shape.Quad(new Vec2(510, 100), new Vec2(540, 110), new Vec2(560, 180), new Vec2(580, 150), new Shape.Mode(3)));
        Shape.Poly p = Shape.createRectOutline(new Vec2(700, 100), new Vec2(100, 50), 15, new Shape.Mode(3));
        sb_main.pushSeparatedPolygon(p);

        Shape.Poly p2 = new Shape.Poly(new Vec2(100, 250), new Shape.Mode(Color.RED), new Vec2(50, 50), new Vec2(-50, 0), new Vec2(50, 0), new Vec2(-50, 50), new Vec2(0, -50));
        Shape.sortPoints(p2);
        sb_main.pushSeparatedPolygonSorted(p2);

        vb_main.bufferData(sb_main.getSetVertices());
        va_main.pushBuffer(vb_main, VertexArray.Layout.getDefaultLayout());

        textRenderer.setupBufferObjects();
        to1 = new TextRenderer.TextObject(1, "", new Vec2());
        to1.setBgColour(Color.BLACK);
        textRenderer.pushTextObject(to1);

        // CIRCLE BUFFERS
        vb_circles.genId();
        va_circles.genId();

        sb_circles.setAdditionalVerts(5);
        sb_circles.pushCircle(new Vec2(200), 50, Color.BLUE);
        sb_circles.pushCircleOutline(new Vec2(300), 34, 10, Color.GREEN);

        VertexArray.Layout instanceLayout = new VertexArray.Layout();
        instanceLayout.pushFloat(2);  // circle pos
        instanceLayout.pushFloat(1);  // radius
        instanceLayout.pushFloat(1);  // inner radius
        instanceLayout.pushFloat(3);  // colour
        vb_circles.bufferData(sb_circles.getSetVertices());
        va_circles.pushBuffer(vb_circles, instanceLayout, 1);

    }

    /** Must be called after window is visible */
    public void setupShaders() {
        sh_main.genProgram();
        sh_main.attachShaders(Constants.SHADER_VERTEX, Constants.SHADER_FRAGMENT);
        sh_main.linkProgram();

        ShaderHelper.uniform2f(sh_main, "resolution", Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);

        new Texture("res/textures/explosion.png").bind(1, sh_main);
        new Texture("res/textures/closed.png").bind(2, sh_main);

        sh_circles.genProgram();
        sh_circles.attachShaders("res/shaders/vs_cir.vert", "res/shaders/fs_cir.frag");
        sh_circles.linkProgram();

        ShaderHelper.uniform2f(sh_circles, "resolution", Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);
    }

    public void updateFpsCounter() {
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
        ShaderHelper.uniform1i(sh_circles, "debugMode", debugMode ? 1:0);
    }

    public void render() {
        Renderer.clearScreen();
        ShaderHelper.uniform1f(sh_main, "time", (float) glfwGetTime());

        Renderer.draw(debugMode ? GL_LINE_STRIP : GL_TRIANGLE_STRIP, va_main, sb_main.getVertexCount());
        Renderer.draw(textRenderer);

        sh_circles.bind();
        Renderer.drawInstanced(GL_TRIANGLES, va_circles, 3, sb_circles.getVertexCount());

        Renderer.finish(window);
    }

    public double mainLoop(double dt) {
        double tStart = System.nanoTime();
        frameCounter++;

        glfwPollEvents();
        updateFpsCounter();
        to1.setString("Secs Elapsed: %s, FPS: %s\nDebug (tab): %s\ns: %s, v: %s, f: %s/%s (%.5f)", secondsElapsed, fps, debugMode, sb_main.getSeparationsCount(), sb_main.getVertexCount(), sb_main.getFloatCount(), sb_main.getBufferSize(), sb_main.getCurrentFullnessPercent());
        render();

        return MathUtils.nanoToSecond(System.nanoTime() - tStart);
    }
}
