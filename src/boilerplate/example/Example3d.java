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
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;

public class Example3d extends GameBase {
    public boilerplate.common.Window window = new Window();
    final Dimension SCREEN_SIZE = new Dimension(800, 800);

    boolean renderWireFrame = false;

    Camera3d camera = new Camera3d(Camera3d.MODE_FLY, new Vector3f(0, 0, 3));

    ShaderProgram sh = new ShaderProgram();
    ShaderProgram shOutline = new ShaderProgram();
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
        Renderer.enableStencilTest();
        Renderer.setStencilOperation(GL_KEEP, GL_KEEP, GL_REPLACE);
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

        sh.autoInitializeShadersMulti("shaders/3d.glsl");
        shOutline.autoInitializeShadersMulti("shaders/3d_outline.glsl");

        Matrix4f projection = new Matrix4f().identity();
        projection.perspective((float) Math.toRadians(80), (float) SCREEN_SIZE.width / (float) SCREEN_SIZE.height, .1f, 50);
        vub.bindUniformBlock(sh, "ViewBlock");
        vub.bindUniformBlock(shOutline, "ViewBlock");
        vub.bufferSize(MathUtils.MATRIX4F_BYTES_SIZE * 2);
        vub.bufferSubData(0, MathUtils.matrixToBuff(projection));

        VertexArray.Layout l = new VertexArray.Layout();
        l.pushFloat(3);
        l.pushFloat(2);
        va.bindBuffers(vb, veb);
        va.pushLayout(l);

        BufferBuilder3f bb = new BufferBuilder3f();
        bb.setAdditionalVertFloats(2);

        Shape3d.Poly3d poly = Shape3d.createCube(new Vector3f(), 1);
        poly.mode = new ShapeMode.Unpack(new float[] {0, 0}, new float[] {1, 0}, new float[] {1, 1}, new float[] {0, 1});
        bb.pushPolygon(poly);
        vb.bufferData(bb);
        veb.bufferData(poly.elementIndex);

        new Texture("textures/breaking.png").bind();
    }

    public void render() {
        float time = (float) glfwGetTime();

        glStencilFunc(GL_ALWAYS, 1, 0xFF);  // write 1 to all fragments that pass
        glStencilMask(0xFF);  // enable writing
        Renderer.clearScreen();

        // update camera
        if (camera.hasChanged) {
            camera.hasChanged = false;
            vub.bufferSubData(MathUtils.MATRIX4F_BYTES_SIZE, MathUtils.matrixToBuff(camera.generateViewMatrix()));
        }

        Matrix4f model1 = new Matrix4f().identity();
        Matrix4f model2 = new Matrix4f().identity();
//        model2.rotateX(time * (float) Math.toRadians(120));
//        model2.rotateY(time * (float) Math.toRadians(70));
        model2.translate((float) Math.cos(time) * 1.2f, 0, (float) Math.sin(time) * 1.2f);
        model2.scale(.8f, .5f, .5f);

        float[] m1 = new float[4*4];
        float[] m2 = new float[4*4];
        model1.get(m1);
        model2.get(m2);

        float m1Dist = camera.pos.sub(m1[12], m1[13], m1[14], new Vector3f()).lengthSquared();
        float m2Dist = camera.pos.sub(m2[12], m2[13], m2[14], new Vector3f()).lengthSquared();
        System.out.println(m2Dist);
        boolean m1First = m1Dist > m2Dist;  // further away should be rendered first if transparent

        drawObjects(model1, model2, sh, m1First);

        glStencilFunc(GL_NOTEQUAL, 1, 0xFF);  // only draw if fragment in stencil is NOT equal to 1
        glStencilMask(0x00);  // disable writing
        drawObjects(model1.scale(1.2f), model2.scale(1.2f), shOutline, m1First);

        Renderer.finish(window);
    }

    private void drawObjects(Matrix4f model1, Matrix4f model2, ShaderProgram sh, boolean m1First) {
        sh.uniformMatrix4f("model", m1First ? model1 : model2);
        Renderer.drawElements(renderWireFrame ? GL_LINES : GL_TRIANGLES, va, veb, 36);

        sh.uniformMatrix4f("model", m1First ? model2 : model1);
        Renderer.drawElements(renderWireFrame ? GL_LINES : GL_TRIANGLES, va, veb, 36);
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
