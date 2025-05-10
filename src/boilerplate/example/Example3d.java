package boilerplate.example;

import boilerplate.common.BoilerplateConstants;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import boilerplate.rendering.*;
import boilerplate.rendering.text.FontManager;
import boilerplate.rendering.text.TextRenderer;
import boilerplate.utility.Logging;
import boilerplate.utility.MathUtils;
import boilerplate.utility.Vec2;
import boilerplate.utility.Vec3;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;

public class Example3d extends GameBase {
    public boilerplate.common.Window window = new Window();
    final Dimension SCREEN_SIZE = new Dimension(800, 800);

    boolean[] heldKeys = new boolean[350];

    Vec3 worldUp = new Vec3(0, 1, 0);
    Vec3 camPos = new Vec3();
    Vec3 camRot = new Vec3();
    Vec3 front = new Vec3();
    Vec3 right = new Vec3();
    Vec3 up = new Vec3();

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
                -.5f,  .5f, -5,    1, 1, 1,  // tl
                 .5f,  .5f, -5,    1, 0, 0,  // tr
                 .5f, -.5f, -5,    0, 1, 0,  // br
                -.5f, -.5f, -5,    0, 0, 1,  // bl

                // back quad
                -.5f,  .5f, -6,   1, 0, 0,
                 .5f,  .5f, -6,   0, 1, 0,
                 .5f, -.5f, -6,   0, 0, 1,
                -.5f, -.5f, -6,   1, 1, 1
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
//        glViewport(0, 0, SCREEN_SIZE.width, SCREEN_SIZE.height);
        sh.bind();
        Renderer.drawElements(GL_TRIANGLES, va, veb, 36);
        Renderer.finish(window);
    }

    public void updateCameraPos(double dt) {
        float mul = 1;
        if (heldKeys[GLFW_KEY_LEFT_SHIFT]) mul = 3;

        // rotation
        float rAdd = .02f * mul;  // radians
        if (heldKeys[GLFW_KEY_DOWN]) camRot.y += rAdd;
        if (heldKeys[GLFW_KEY_UP]) camRot.y -= rAdd;
        camRot.y = (float) Math.clamp(camRot.y, -Math.PI * .5, Math.PI * .5);

        if (heldKeys[GLFW_KEY_RIGHT]) camRot.x += rAdd;
        if (heldKeys[GLFW_KEY_LEFT]) camRot.x -= rAdd;

        if (heldKeys[GLFW_KEY_O]) camRot.z += rAdd;
        if (heldKeys[GLFW_KEY_P]) camRot.z -= rAdd;

        front.set(
                (float) (Math.cos(camRot.yaw()) * Math.cos(camRot.pitch())),
                (float) Math.sin(camRot.pitch()),
                (float) (Math.sin(camRot.yaw()) * Math.cos(camRot.pitch()))
        );
        right = front.cross(worldUp);
        up = right.cross(front);
        front.normaliseSelf();
        right.normaliseSelf();
        up.normaliseSelf();

        // position
        float pAdd = (float) (5 * dt) * mul;
        Vec3 vel = new Vec3();
        if (heldKeys[GLFW_KEY_D]) vel.addSelf(right.mul(pAdd));
        if (heldKeys[GLFW_KEY_A]) vel.subSelf(right.mul(pAdd));

//        if (heldKeys[GLFW_KEY_E]) vel.y += pAdd;
//        if (heldKeys[GLFW_KEY_Q]) vel.y -= pAdd;
//
//        if (heldKeys[GLFW_KEY_S]) vel.addSelf(front.mul(pAdd));
//        if (heldKeys[GLFW_KEY_W]) vel.subSelf(front.mul(pAdd));
        camPos.addSelf(vel);
    }

    @Override
    public void mainLoop(double staticDt) {
        sh.uniform3f("camPos", camPos.x, camPos.y, camPos.z);
        sh.uniform3f("camRot", camRot.x, camRot.y, camRot.z);
        sh.uniform1f("time", (float) glfwGetTime());
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
