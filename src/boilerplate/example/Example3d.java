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

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;

public class Example3d extends GameBase {
    public boilerplate.common.Window window = new Window();
    final Dimension SCREEN_SIZE = new Dimension(500, 500);

    ShaderHelper sh = new ShaderHelper();
    VertexArray va = new VertexArray();
    VertexBuffer vb = new VertexBuffer();
    VertexElementBuffer veb = new VertexElementBuffer(VertexElementBuffer.TYPE_INT);

    @Override
    public void start() {
        TimeStepper.startTimeStepper(BoilerplateConstants.DT, this);
    }

    @Override
    public void createCapabilitiesAndOpen() {
        Window.Options winOps = new Window.Options();
        winOps.title = "the 3d example";
        winOps.initWindowSize = SCREEN_SIZE;
        window.quickSetupAndShow(winOps);

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
        va.genId();
        vb.genId();
        veb.genId();

        sh.autoInitializeShadersMulti("shaders/e.glsl");
        sh.uniformResolutionData(SCREEN_SIZE, BoilerplateConstants.create2dProjectionMatrix(SCREEN_SIZE));

        VertexArray.Layout l = new VertexArray.Layout();
        l.pushFloat(3);
        l.pushFloat(3);
        va.bindBuffers(vb, veb);
        va.pushLayout(l);

        vb.bufferData(new float[] {
                -.5f,  .5f, 0,     1, 1, 1,
                 .5f,  .5f, 0,     1, 0, 0,
                 .5f, -.5f, 0,     0, 1, 1,
                -.5f, -.5f, 0,     1, 1, 0
        });
        veb.bufferData(new int[] {
                0, 1, 2,
                0, 3, 2,
        });
    }

    public void render() {
        Renderer.clearScreen();
        sh.bind();
        Renderer.drawElements(GL_TRIANGLES, va, veb, 6);
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
