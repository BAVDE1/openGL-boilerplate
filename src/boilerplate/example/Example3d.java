package boilerplate.example;

import boilerplate.common.BoilerplateConstants;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import boilerplate.rendering.*;
import boilerplate.utility.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;

public class Example3d extends GameBase {
    public boilerplate.common.Window window = new Window();
    final Dimension SCREEN_SIZE = new Dimension(800, 800);

    boolean renderWireFrame = false;

    Camera3d camera = new Camera3d(Camera3d.MODE_FLY, new Vector3f(0, 0, 3));

    ShaderProgram sh = new ShaderProgram();
    VertexArray va = new VertexArray();
    VertexBuffer vb = new VertexBuffer();
    VertexElementBuffer veb = new VertexElementBuffer(VertexElementBuffer.ELEMENT_TYPE_INT);

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

        Renderer.enableDepthTest();
        Renderer.enableFaceCullingDefault();
        glViewport(0, 0, SCREEN_SIZE.width, SCREEN_SIZE.height);

        bindEvents();
        setupBuffers();
    }

    public void bindEvents() {
        glDebugMessageCallback(Logging.debugCallback(), -1);

        glfwSetKeyCallback(window.handle, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_ESCAPE) this.window.setToClose();
                if (key == GLFW_KEY_TAB) renderWireFrame = !renderWireFrame;
                if (key == GLFW_KEY_F)
                    camera.setMode(camera.getMode() == Camera3d.MODE_FLY ? Camera3d.MODE_TARGET : Camera3d.MODE_FLY);
            }
        });

        glfwSetMouseButtonCallback(window.handle, (window, button, action, mods) -> {
            camera.processMouseInputs(this.window);
        });

        glfwSetCursorPosCallback(window.handle, (window, xPos, yPos) -> {
            camera.processMouseMovement(this.window, (float) xPos, (float) yPos);
        });

        glfwSetScrollCallback(window.handle, (window, xDelta, yDelta) -> {
            camera.processScroll(this.window, (float) xDelta, (float) yDelta);
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
        l.pushFloat(2);
        va.bindBuffers(vb, veb);
        va.pushLayout(l);

        vb.bufferData(new float[]{
                // front quad
                -.5f, .5f, .5f, 0, 0,  // tl
                .5f, .5f, .5f, 1, 0,  // tr
                .5f, -.5f, .5f, 1, 1,  // br
                -.5f, -.5f, .5f, 0, 1,  // bl

                // back quad
                -.5f, .5f, -.5f, 1, 1,
                .5f, .5f, -.5f, 0, 1,
                .5f, -.5f, -.5f, 0, 0,
                -.5f, -.5f, -.5f, 1, 0
        });
        veb.bufferData(new int[]{
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

        new Texture("textures/breaking.png").bind();
    }

    public void render() {
        float time = (float) glfwGetTime();

        Renderer.clearScreen();

        Matrix4f model = new Matrix4f().identity();
        Matrix4f view = camera.generateViewMatrix();
        Matrix4f projection = new Matrix4f().identity();
        projection.perspective((float) Math.toRadians(80), (float) SCREEN_SIZE.width / (float) SCREEN_SIZE.height, .1f, 100);

        sh.uniformMatrix4f("model", model);
        sh.uniformMatrix4f("view", view);
        sh.uniformMatrix4f("projection", projection);

        sh.bind();
        Renderer.drawElements(renderWireFrame ? GL_LINES : GL_TRIANGLES, va, veb, 36);

        model = new Matrix4f().identity();
        model.rotateX(time * (float) Math.toRadians(120));
        model.rotateY(time * (float) Math.toRadians(70));
        model.translate(1.2f, 0, 0);
        model.scale(.8f, .5f, .5f);
        sh.uniformMatrix4f("model", model);
        Renderer.drawElements(renderWireFrame ? GL_LINES : GL_TRIANGLES, va, veb, 36);
        Renderer.finish(window);
    }

    @Override
    public void mainLoop(double staticDt) {
        glfwPollEvents();
        camera.processKeyInputs(window, staticDt);
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
