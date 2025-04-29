package boilerplate.example;

import boilerplate.common.BoilerplateConstants;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import boilerplate.rendering.Renderer;
import boilerplate.rendering.text.FontManager;
import boilerplate.rendering.text.TextRenderer;
import boilerplate.utility.Logging;
import boilerplate.utility.Vec2;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;

public class Example3d extends GameBase {
    public boilerplate.common.Window window = new Window();
    final Dimension SCREEN_SIZE = new Dimension(500, 500);
    TextRenderer textRenderer = new TextRenderer();

    @Override
    public void start() {
        TimeStepper.startTimeStepper(BoilerplateConstants.DT, this);
    }

    @Override
    public void createCapabilitiesAndOpen() {
        Window.Options winOps = new Window.Options();
        winOps.title = "the 3d example";
        winOps.initWindowSize = SCREEN_SIZE;
        window.setOptions(winOps);
        window.setup();
        Renderer.setupGLContext();
        window.show();

        FontManager.init();
        FontManager.loadFont(Font.MONOSPACED, Font.BOLD, 20, true);
        FontManager.generateAndBindAllFonts(SCREEN_SIZE, BoilerplateConstants.create2dProjectionMatrix(SCREEN_SIZE));

        bindEvents();
        setupBuffers();
    }

    public void bindEvents() {
        glDebugMessageCallback(Logging.debugCallback(), -1);

        glfwSetKeyCallback(window.handle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) this.window.setToClose();
        });
    }

    public void setupBuffers() {
        TextRenderer.TextObject to1 = new TextRenderer.TextObject(1, "3 dimensions!?!??!", new Vec2(120, 50), Color.RED, Color.BLACK);
        to1.setAlignment(TextRenderer.TextObject.ALIGN_MIDDLE);
        textRenderer.setupBufferObjects();
        textRenderer.pushTextObject(to1);
    }

    public void render() {
        Renderer.clearScreen();
        Renderer.draw(textRenderer);
        Renderer.finish(window);
    }

    @Override
    public void mainLoop(double staticDt) {
        glfwPollEvents();
        render();
    }

    @Override
    public boolean shouldClose() {
        return glfwWindowShouldClose(window.handle);
    }

    @Override
    public void close() {
        window.close();
    }
}
