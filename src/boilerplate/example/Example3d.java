package boilerplate.example;

import boilerplate.common.BoilerplateConstants;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import boilerplate.rendering.*;
import boilerplate.rendering.builders.BufferBuilder3f;
import boilerplate.rendering.builders.Shape3d;
import boilerplate.rendering.builders.ShapeMode;
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
    VertexUniformBuffer vub = new VertexUniformBuffer();

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
            camera.processMouseScroll(this.window, (float) xDelta, (float) yDelta);
        });
    }

    public void setupBuffers() {
        va.genId();
        vb.genId();
        veb.genId();
        vub.genId();

        sh.autoInitializeShadersMulti("shaders/e.glsl");
        sh.uniformResolutionData(SCREEN_SIZE, BoilerplateConstants.create2dProjectionMatrix(SCREEN_SIZE));

        Matrix4f projection = new Matrix4f().identity();
        projection.perspective((float) Math.toRadians(80), (float) SCREEN_SIZE.width / (float) SCREEN_SIZE.height, .1f, 5);
        vub.bindUniformBlock(sh, "ViewBlock");
        vub.bufferSize(MathUtils.MATRIX4F_BYTES_SIZE * 2);
        vub.bufferSubData(0, MathUtils.matrixToBuff(projection));

        VertexArray.Layout l = new VertexArray.Layout();
        l.pushFloat(3);
        l.pushFloat(2);
        va.bindBuffers(vb, veb);
        va.pushLayout(l);

        BufferBuilder3f bb = new BufferBuilder3f();
        bb.setAdditionalVertFloats(2);

        Shape3d.Poly3d poly = Shape3d.createTri(new Vector3f(), 1, 1, .5f, 1);
        poly.mode = new ShapeMode.Unpack(new float[] {0, 0}, new float[] {1, 0}, new float[] {1, 1}, new float[] {0, 1});
        bb.pushPolygon(poly);
        vb.bufferData(bb);
        veb.bufferData(poly.elementIndex);

        new Texture("textures/breaking.png").bind();
    }

    public void render() {
        float time = (float) glfwGetTime();
        Renderer.clearScreen();

        // update camera
        if (camera.hasChanged) {
            camera.hasChanged = false;
            vub.bufferSubData(MathUtils.MATRIX4F_BYTES_SIZE, MathUtils.matrixToBuff(camera.generateViewMatrix()));
        }

        Matrix4f model = new Matrix4f().identity();
        sh.uniformMatrix4f("model", model);

        sh.bind();
        Renderer.drawElements(renderWireFrame ? GL_LINES : GL_TRIANGLES, va, veb, 24);

        model = new Matrix4f().identity();
        model.rotateX(time * (float) Math.toRadians(120));
        model.rotateY(time * (float) Math.toRadians(70));
        model.translate(1.4f, 0, 0);
        model.scale(.8f, .5f, .5f);
        sh.uniformMatrix4f("model", model);
        Renderer.drawElements(renderWireFrame ? GL_LINES : GL_TRIANGLES, va, veb, 24);
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
