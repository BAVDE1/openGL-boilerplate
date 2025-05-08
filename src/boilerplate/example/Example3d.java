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
import boilerplate.utility.Vec3;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;

public class Example3d extends GameBase {
    public boilerplate.common.Window window = new Window();
    final Dimension SCREEN_SIZE = new Dimension(800, 800);

    int[] heldKeys = new int[350];
    Vec3 camPos = new Vec3();
    Vec3 camRot = new Vec3();

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
            if (action == GLFW_PRESS) {
                heldKeys[key] = 1;
                if (key == GLFW_KEY_ESCAPE) this.window.setToClose();
            }

            if (action == GLFW_RELEASE) {
                heldKeys[key] = 0;
            }
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
                -.5f,  .5f, -5,    1, 1, 1,  // tl
                 .5f,  .5f, -5,    1, 0, 0,  // tr
                 .5f, -.5f, -5,    0, 1, 1,  // br
                -.5f, -.5f, -5,    1, 1, 0,  // bl

                -.5f,  .5f, -6f,   1, 0, 0,
                 .5f,  .5f, -6f,   0, 1, 1,
                 .5f, -.5f, -6f,   1, 1, 0,
                -.5f, -.5f, -6f,   1, 1, 1
        });
        veb.bufferData(new int[] {
                0, 1, 2,  // front
                0, 3, 2,

                4, 5, 1,  // top
                4, 0, 1,

                4, 0, 3,  // left
                4, 7, 3,

                1, 5, 6,  // right
                1, 2, 6,

                3, 2, 6,  // bottom
                3, 7, 6,

                4, 5, 6,  // back
                4, 7, 6,
        });
    }

    public void render() {
        Renderer.clearScreen();
        sh.bind();
        Renderer.drawElements(GL_TRIANGLES, va, veb, 36);
        Renderer.finish(window);
    }

    public void updateCameraPos(double dt) {
        float mul = 1;
        if (heldKeys[GLFW_KEY_LEFT_SHIFT] == 1) mul = 5;

        float amnt = (float) (5 * dt) * mul;
        if (heldKeys[GLFW_KEY_D] == 1) camPos.x += amnt;
        if (heldKeys[GLFW_KEY_A] == 1) camPos.x -= amnt;

        if (heldKeys[GLFW_KEY_E] == 1) camPos.y += amnt;
        if (heldKeys[GLFW_KEY_Q] == 1) camPos.y -= amnt;

        if (heldKeys[GLFW_KEY_S] == 1) camPos.z += amnt;
        if (heldKeys[GLFW_KEY_W] == 1) camPos.z -= amnt;

        if (heldKeys[GLFW_KEY_UP] == 1) camRot.x += .01f;
        if (heldKeys[GLFW_KEY_DOWN] == 1) camRot.x -= .01f;

        if (heldKeys[GLFW_KEY_LEFT] == 1) camRot.y += .01f;
        if (heldKeys[GLFW_KEY_RIGHT] == 1) camRot.y -= .01f;
    }

    @Override
    public void mainLoop(double staticDt) {
        sh.uniform3f("camPos", camPos.x, camPos.y, camPos.z);
        sh.uniform3f("camRot", camRot.x, camRot.y, camRot.z);
        glfwPollEvents();
        updateCameraPos(staticDt);
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
