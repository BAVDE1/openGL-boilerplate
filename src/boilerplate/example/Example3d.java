package boilerplate.example;

import boilerplate.common.BoilerplateConstants;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import boilerplate.rendering.*;
import boilerplate.rendering.text.FontManager;
import boilerplate.rendering.text.TextRenderer;
import boilerplate.utility.*;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;

public class Example3d extends GameBase {
    public boilerplate.common.Window window = new Window();
    final Dimension SCREEN_SIZE = new Dimension(800, 800);

    boolean[] heldKeys = new boolean[350];

    Vector3f worldUp = new Vector3f(0, 1, 0);
    Vector3f camPos = new Vector3f();
    Vector3f camRot = new Vector3f();
    Vector3f front = new Vector3f();
    Vector3f right = new Vector3f();
    Vector3f up = new Vector3f();

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
                heldKeys[key] = true;
                if (key == GLFW_KEY_ESCAPE) this.window.setToClose();
            }

            if (action == GLFW_RELEASE) {
                heldKeys[key] = false;
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
                // front quad
                -.5f,  .5f, .5f,    1, 1, 1,  // tl
                 .5f,  .5f, .5f,    1, 0, 0,  // tr
                 .5f, -.5f, .5f,    0, 1, 0,  // br
                -.5f, -.5f, .5f,    0, 0, 1,  // bl

                // back quad
                -.5f,  .5f, -.5f,   1, 0, 0,
                 .5f,  .5f, -.5f,   0, 1, 0,
                 .5f, -.5f, -.5f,   0, 0, 1,
                -.5f, -.5f, -.5f,   1, 1, 1
        });
        veb.bufferData(new int[] {
                0, 1, 2,  // front
                0, 2, 3,

                4, 5, 1,  // top
                4, 1, 0,

                4, 0, 3,  // left
                4, 3, 7,

                1, 5, 6,  // right
                1, 6, 2,

                3, 2, 6,  // bottom
                3, 6, 7,

                6, 5, 4,  // back
                6, 4, 7,
        });
    }

    public void render() {
        Renderer.clearScreen();

        glViewport(0, 0, SCREEN_SIZE.width, SCREEN_SIZE.height);
        Matrix4f model = new Matrix4f().identity();
        model.rotation((float) (glfwGetTime() * Math.toRadians(200)), .5f, 1, 0);
//        model.rotation((float) Math.toRadians(-55), 1, 0, 0);
        Matrix4f view = new Matrix4f().identity();
        view.translate(0, 0, -3f);
        Matrix4f proj = new Matrix4f().perspective((float) Math.toRadians(45), (float) SCREEN_SIZE.width / SCREEN_SIZE.height, .1f, 1000);

        sh.uniformMatrix4f("model", model);
        sh.uniformMatrix4f("view", view);
        sh.uniformMatrix4f("projection", proj);

        sh.bind();
        Renderer.drawElements(GL_TRIANGLES, va, veb, 36);
        Renderer.finish(window);
    }

    public void updateCameraPos(double dt) {
    }

    @Override
    public void mainLoop(double staticDt) {
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
