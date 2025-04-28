package boilerplate.example;

import boilerplate.common.BoilerplateConstants;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import boilerplate.rendering.*;
import boilerplate.rendering.text.FontManager;
import boilerplate.rendering.text.TextRenderer;
import boilerplate.utility.Logging;
import boilerplate.utility.Vec2;

import static org.lwjgl.opengl.GL45.*;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

public class ExampleIndex extends GameBase {
    public boilerplate.common.Window window = new Window();
    Dimension screenSize = new Dimension(500, 200);
    TextRenderer textRenderer = new TextRenderer();
    VertexBuffer vb;

    @Override
    public void start() {
        TimeStepper.startTimeStepper(BoilerplateConstants.DT, this);
    }

    @Override
    public void createCapabilitiesAndOpen() {
        Window.Options winOps = new Window.Options();
        winOps.title = "the example index";
        winOps.initWindowSize = screenSize;
        window.setOptions(winOps);
        window.setup();
        Renderer.setupGLContext();
        window.show();

        FontManager.init();
        FontManager.loadFont(Font.MONOSPACED, Font.BOLD, 20, true);
        FontManager.generateAndBindAllFonts(screenSize, new float[] {
                2f/screenSize.width,  0,                       0,  -1,
                0,                    2f/-screenSize.height,   0,   1,
                0,                    0,                      -1,   0,
                0,                    0,                       0,   1
        });

        bindEvents();
        setupBuffers();
    }

    public void bindEvents() {
        glDebugMessageCallback(Logging.debugCallback(), -1);

        glfwSetKeyCallback(window.handle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);

            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_Q) open2dExample();
                if (key == GLFW_KEY_E) open3dExample();
            }
        });
    }

    public void setupBuffers() {
        TextRenderer.TextObject to1 = new TextRenderer.TextObject(1, "[q]\n\n2d example", new Vec2(120, 50), Color.WHITE, Color.BLACK);
        TextRenderer.TextObject to2 = new TextRenderer.TextObject(1, "[e]\n\n3d example", new Vec2(screenSize.width-120, 50), Color.GRAY, Color.BLACK);
        to1.setAlignment(TextRenderer.TextObject.ALIGN_MIDDLE);
        to2.setAlignment(TextRenderer.TextObject.ALIGN_MIDDLE);
        textRenderer.setupBufferObjects();
        textRenderer.pushTextObject(to1, to2);
    }

    public void open2dExample() {
        close();
        Logging.mystical("Deleting GL values...");
        textRenderer.delete();
        FontManager.deleteAll();
        Texture.deleteAll();
        Renderer.unbindAll();
        Logging.mystical("Opening 2d example");
        new Example2d().start();
    }

    public void open3dExample() {
        Logging.warn("3d example doesn't exist rn");
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
