package boilerplate.example;

import boilerplate.common.BoilerplateConstants;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import org.lwjgl.opengl.GL45;
import boilerplate.rendering.Shape;
import boilerplate.rendering.*;
import boilerplate.rendering.text.FontManager;
import boilerplate.rendering.text.TextRenderer;
import boilerplate.utility.Logging;
import boilerplate.utility.MathUtils;
import boilerplate.utility.Vec2;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Manages everything. Contains the main loop.
 */
public class ExampleGame extends GameBase {
    public Window window = new Window();

    public static boolean debugMode = false;

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
    TextRenderer.TextObject to2;
    TextRenderer textRenderer = new TextRenderer();

    Vec2 mousePos = new Vec2();
    Vec2 mousePosOnClick = new Vec2();

    Vec2 viewPos = new Vec2();
    float viewScale = 1f;
    float scaleAddition = .1f;
    boolean forceUpdateView = false;

    int[] heldMouseKeys = new int[8];
    int[] heldKeys = new int[350];

    double timeStarted = 0;
    int secondsElapsed = 0;
    int frameCounter = 0;
    int fps = 0;

    public void start() {
        timeStarted = System.currentTimeMillis();
        TimeStepper.startTimeStepper(BoilerplateConstants.DT, this);
    }

    public void createCapabilitiesAndOpen() {
        window.setup();
        Renderer.setupGLContext();
        window.show();

        FontManager.init();
        FontManager.loadFont(Font.MONOSPACED, Font.BOLD, 14, true);
        FontManager.generateAndBindAllFonts();

        bindEvents();
        setupShaders();
        setupBuffers();
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
                heldKeys[key] = 1;

                switch (key) {
                    case GLFW_KEY_E -> addToViewScale(-scaleAddition, false);
                    case GLFW_KEY_Q -> addToViewScale(scaleAddition, false);
                    case GLFW_KEY_R -> {
                        if (heldMouseKeys[GLFW_MOUSE_BUTTON_1] == 1) break;
                        viewPos.set(0);
                        viewScale = 1;
                        forceUpdateView = true;
                    }

                    case GLFW_KEY_TAB -> toggleDebug();
                }
            }

            if (action == GLFW_RELEASE) {
                heldKeys[key] = 0;
            }
        });

        glfwSetMouseButtonCallback(window.handle, (window, button, action, mode) -> {
            if (action == GLFW_PRESS) {
                heldMouseKeys[button] = 1;

                if (button == GLFW_MOUSE_BUTTON_1) {
                    mousePosOnClick.set(mousePos.add(viewPos));
                }
            }

            if (action == GLFW_RELEASE) {
                heldMouseKeys[button] = 0;
            }
        });

        glfwSetScrollCallback(window.handle, (window, xOffset, yOffset) -> {
            if (yOffset != 0.0) addToViewScale((float) (scaleAddition * Math.clamp(-yOffset, -1, 1)), true);
        });

        glfwSetCursorPosCallback(window.handle, (window, xPos, yPos) -> mousePos.set((float) xPos, (float) yPos));
    }

    public void setupBuffers() {
        // MAIN BUFFERS
        vbMain.genId(); vaMain.genId();

        builderMain.setAdditionalVertFloats(VertexArray.Layout.getDefaultLayoutAdditionalVerts());
        builderMain.pushSeparatedQuad(Shape.createRect(new Vec2(50, 50), new Vec2(500, 100), new Shape.Mode(1, new Vec2(), new Vec2(1))));
        builderMain.pushSeparatedQuad(Shape.createRect(new Vec2(200, 200), new Vec2(700, 150), new Shape.Mode(2, new Vec2(), new Vec2(1))));
        builderMain.pushSeparatedQuad(Shape.createLine(new Vec2(70, 20), new Vec2(150, 150), 20, new Shape.Mode(3)));
        builderMain.pushSeparatedQuad(new Shape.Quad(new Vec2(510, 100), new Vec2(540, 110), new Vec2(560, 180), new Vec2(580, 150), new Shape.Mode(3)));
        builderMain.pushSeparatedPolygon(Shape.createRectOutline(new Vec2(700, 100), new Vec2(100, 50), 15, new Shape.Mode(3)));

        Shape.Poly p2 = new Shape.Poly(new Vec2(100, 250), new Shape.Mode(Color.RED), new Vec2(-50, 50), new Vec2(0, -50), new Vec2(50, 50), new Vec2(-50, 0), new Vec2(50, 0));
        Shape.sortPoints(p2);
        builderMain.pushSeparatedPolygonSorted(p2);

        vbMain.bufferSetData(builderMain);
        vaMain.pushBuffer(vbMain, VertexArray.Layout.createDefaultLayout());

        to1 = new TextRenderer.TextObject(1, "", new Vec2(5), Color.CYAN, Color.BLACK);
        to2 = new TextRenderer.TextObject(1, "", new Vec2(5, 50), Color.WHITE, Color.BLACK);
        to1.setBgMargin(new Vec2(5));
        to2.setBgMargin(new Vec2(5));
        textRenderer.setupBufferObjects();
        textRenderer.pushTextObject(to1, to2);

        // CIRCLE BUFFERS
        vbCircles.genId(); vaCircles.genId();

        builderCircles.setAdditionalVertFloats(5);
        builderCircles.pushCircle(new Vec2(200), 50, Color.BLUE);
        builderCircles.pushCircle(new Vec2(300), 34, 10, Color.GREEN);

        VertexArray.Layout instanceLayout = new VertexArray.Layout();
        instanceLayout.pushFloat(2);  // circle pos
        instanceLayout.pushFloat(1);  // radius
        instanceLayout.pushFloat(1);  // inner radius
        instanceLayout.pushFloat(3);  // colour
        vbCircles.bufferSetData(builderCircles);
        vaCircles.pushBuffer(vbCircles, instanceLayout, 1);
    }

    /** Must be called after window is visible */
    public void setupShaders() {
        shMain.useMainShader();
        shCircles.useCircleShader();

        new Texture("textures/explosion.png").bindToTexArray(2, shMain);
        new Texture("textures/closed.png").bindToTexArray(3, shMain);

        ShaderHelper.uniformResolutionData(shMain);
        ShaderHelper.uniform1f(shMain, "viewScale", viewScale);
        ShaderHelper.uniformResolutionData(shCircles);
        ShaderHelper.uniform1f(shCircles, "viewScale", viewScale);
    }

    public void updateFpsAndDebugText() {
        // updates every second
        int newSeconds = (int) Math.floor(MathUtils.millisToSecond(System.currentTimeMillis()) - MathUtils.millisToSecond(timeStarted));
        if (newSeconds != secondsElapsed) {
            fps = frameCounter;
            frameCounter = 0;
            secondsElapsed = newSeconds;
        }

        // debug string
        BufferBuilder2f textBuff = textRenderer.getBufferBuilder();
        to1.setString("FPS: %s, Elapsed: %s [debug (tab): %s]\nView [pos:%.0f,%.0f, scale:%.2f] (r)eset",
                fps, secondsElapsed, debugMode,
                viewPos.x, viewPos.y, viewScale
        );
        to2.setString("""
                        Buffers:\

                         - text [s:%s, v:%s, f:%s/%s (%.5f)]\

                         - main [s:%s, v:%s, f:%s/%s (%.5f)]\

                         - circles [s:%s, v:%s, f:%s/%s (%.5f)]""",
                textBuff.getSeparationsCount(),  // note: cause we're using the text buffers own values in this text
                textBuff.getVertexCount(),       // object it'll need to re-build itself a few extra times than normal
                textBuff.getFloatCount(),
                textBuff.getBufferSize(),
                textBuff.getCurrentFullnessPercent(),
                builderMain.getSeparationsCount(),
                builderMain.getVertexCount(),
                builderMain.getFloatCount(),
                builderMain.getBufferSize(),
                builderMain.getCurrentFullnessPercent(),
                builderCircles.getSeparationsCount(),
                builderCircles.getVertexCount(),
                builderCircles.getFloatCount(),
                builderCircles.getBufferSize(),
                builderCircles.getCurrentFullnessPercent()
        );
    }

    public void addToViewScale(float addition, boolean relativeToMouse) {
        // mouse or middle of screen
        Vec2 relativeTo = relativeToMouse ? mousePos : Vec2.fromDim(BoilerplateConstants.SCREEN_SIZE).mul(.5f);
        viewPos.addSelf(relativeTo.mul(viewScale).sub(relativeTo.mul(viewScale+addition)));

        viewScale += addition;
        forceUpdateView = true;
    }

    public void updateViewPos(double dt) {
        Vec2 addition = new Vec2();

        // mouse prioritised over keys
        if (heldMouseKeys[GLFW_MOUSE_BUTTON_1] == 1) {
            addition = mousePos.sub(mousePosOnClick).negate();
            addition.subSelf(viewPos);
        } else {
            float amnt = (float) (200 * dt);
            addition.x += (-amnt * heldKeys[GLFW_KEY_A]) + (amnt * heldKeys[GLFW_KEY_D]);
            addition.y += (-amnt * heldKeys[GLFW_KEY_W]) + (amnt * heldKeys[GLFW_KEY_S]);
            addition.mulSelf(heldKeys[GLFW_KEY_LEFT_SHIFT] == 1 ? 2 : 1);
        }

        if (forceUpdateView || !addition.equals(new Vec2())) {
            viewPos.addSelf(addition);
            ShaderHelper.uniform2f(shMain, "viewPos", viewPos.x, viewPos.y);
            ShaderHelper.uniform1f(shMain, "viewScale", viewScale);

            ShaderHelper.uniform2f(shCircles, "viewPos", viewPos.x, viewPos.y);
            ShaderHelper.uniform1f(shCircles, "viewScale", viewScale);
            forceUpdateView = false;
        }
    }

    public void toggleDebug() {
        debugMode = !debugMode;
        ShaderHelper.uniform1i(shMain, "debugMode", debugMode ? 1:0);
        ShaderHelper.uniform1i(shCircles, "debugMode", debugMode ? 1:0);
    }

    public void render() {
        Renderer.clearScreen();

        // shape examples
        shMain.bind();
        ShaderHelper.uniform1f(shMain, "time", (float) glfwGetTime());
        Renderer.draw(debugMode ? GL_LINE_STRIP : GL_TRIANGLE_STRIP, vaMain, builderMain.getVertexCount());

        shCircles.bind();
        Renderer.drawInstanced(GL_TRIANGLES, vaCircles, 3, builderCircles.getVertexCount());

        Renderer.draw(textRenderer);

        // FINISH
        Renderer.finish(window);
    }

    public void mainLoop(double dt) {
        frameCounter++;

        glfwPollEvents();
        updateViewPos(dt);
        updateFpsAndDebugText();
        render();
    }
}
