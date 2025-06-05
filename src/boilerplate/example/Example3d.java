package boilerplate.example;

import boilerplate.common.BoilerplateConstants;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import boilerplate.rendering.Camera3d;
import boilerplate.rendering.Renderer;
import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.SkyBox;
import boilerplate.rendering.buffers.FrameBuffer;
import boilerplate.rendering.buffers.VertexArray;
import boilerplate.rendering.buffers.VertexArrayBuffer;
import boilerplate.rendering.buffers.VertexElementBuffer;
import boilerplate.rendering.builders.BufferBuilder3f;
import boilerplate.rendering.builders.Shape3d;
import boilerplate.rendering.builders.ShapeMode;
import boilerplate.rendering.textures.CubeMap;
import boilerplate.rendering.textures.Image;
import boilerplate.utility.Logging;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;

public class Example3d extends GameBase {
    public boilerplate.common.Window window = new Window();
    public final static Dimension SCREEN_SIZE = new Dimension(800, 800);

    boolean renderWireFrame = false;

    Camera3d camera = new Camera3d(new Dimension(1, 1), Camera3d.MODE_TARGET, new Vector3f(0, 0, 3));

    ShaderProgram finalSh = new ShaderProgram();
    VertexArray finalVa = new VertexArray();
    VertexArrayBuffer finalVb = new VertexArrayBuffer();

    ShaderProgram sh = new ShaderProgram();
    ShaderProgram shOutline = new ShaderProgram();
    VertexArray va = new VertexArray();
    VertexArrayBuffer vb = new VertexArrayBuffer();
    VertexElementBuffer veb = new VertexElementBuffer(VertexElementBuffer.ELEMENT_TYPE_INT);
    CubeMap ballerCube = new CubeMap();

    SkyBox skyBox = new SkyBox();

    FrameBuffer fb = new FrameBuffer(SCREEN_SIZE);

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
        Renderer.useDefaultFaceCulling();
        glViewport(0, 0, SCREEN_SIZE.width, SCREEN_SIZE.height);
        Image.flipOnSTBLoad();

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
        ballerCube.genId();
        ballerCube.loadFaces("res/textures/space_skybox/px.png", "res/textures/space_skybox/nx.png", "res/textures/space_skybox/py.png", "res/textures/space_skybox/ny.png", "res/textures/space_skybox/pz.png", "res/textures/space_skybox/nz.png");
        ballerCube.useLinearInterpolation();
        ballerCube.useClampEdgeWrap();
        CubeMap.unbind();
        va.genId();
        vb.genId();
        veb.genId();

        sh.autoInitializeShadersMulti("shaders/3d.glsl");
        shOutline.autoInitializeShadersMulti("shaders/3d_outline.glsl");

        camera.setupUniformBuffer(sh, shOutline);
        skyBox.setupBuffers(camera, "res/textures/space_skybox", "png");

        va.fastSetup(new int[]{3, 3}, vb, veb);
        BufferBuilder3f bb = new BufferBuilder3f(true, 3);

        Shape3d.Poly3d poly = Shape3d.createCube(new Vector3f(), 1);
        poly.mode = new ShapeMode.Unpack(
                new float[]{-1, -1, 1}, new float[]{1, -1, 1}, new float[]{1, 1, 1}, new float[]{-1, 1, 1},
                new float[]{-1, -1, -1}, new float[]{1, -1, -1}, new float[]{1, 1, -1}, new float[]{-1, 1, -1}
        );
        bb.pushPolygon(poly);
        vb.bufferData(bb);
        veb.bufferData(poly.elementIndex);

        finalSh.autoInitializeShadersMulti("shaders/3d_final.glsl");
        finalVa.genId();
        finalVb.genId();

        finalVa.fastSetup(new int[]{2, 2}, finalVb);
        finalVb.bufferData(new float[]{
                1, 1, 1, 1,
                -1, 1, 0, 1,
                1, -1, 1, 0,
                -1, -1, 0, 0
        });

        fb.genId();
        fb.attachColourBuffer(fb.setupDefaultColourBuffer());
        fb.attachRenderBuffer(fb.setupDefaultRenderBuffer());
        fb.checkCompletionOrError();
        FrameBuffer.unbind();
    }

    public void render() {
        float time = (float) glfwGetTime();

        Matrix4f model1 = new Matrix4f().identity();
        Matrix4f model2 = new Matrix4f().identity();
        model2.rotateX(time * (float) Math.toRadians(120));
        model2.rotateY(time * (float) Math.toRadians(70));
        model2.translate(0, 0, 1.2f);
        model2.scale(.8f, .5f, .5f);

        // --- 3D SPACE --- //
        fb.bind();
        Renderer.enableStencilTest();
        Renderer.setStencilFunc(GL_ALWAYS, 1, true);  // write 1 to all fragments that pass
        Renderer.enableStencilWriting();

        Renderer.clearCDS();
        Renderer.enableDepthTest();

        sh.bind();
        ballerCube.bind();
        drawObjects(model1, model2, sh);

        Renderer.setStencilFunc(GL_NOTEQUAL, 1, true);  // only draw if fragment in stencil is NOT equal to 1
        Renderer.disableStencilWriting();
        drawObjects(model1.scale(1.2f), model2.scale(1.2f), shOutline);

        Renderer.disableStencilTest();
        skyBox.draw();

        // --- POST PROCESSING --- //
        FrameBuffer.unbind();
        Renderer.clearC();
        Renderer.disableDepthTest();

        finalSh.bind();
        fb.colourBuffers.getFirst().bind();
        Renderer.drawArrays(GL_TRIANGLE_STRIP, finalVa, 4);

        Renderer.finish(window);
    }

    private void drawObjects(Matrix4f model1, Matrix4f model2, ShaderProgram sh) {
        sh.uniformMatrix4f("model", model1);
        Renderer.drawElements(renderWireFrame ? GL_LINES : GL_TRIANGLES, va, veb, 36);

        sh.uniformMatrix4f("model", model2);
        Renderer.drawElements(renderWireFrame ? GL_LINES : GL_TRIANGLES, va, veb, 36);
    }

    @Override
    public void mainLoop(double staticDt) {
        glfwPollEvents();
        camera.processKeyInputs(window, staticDt);
        camera.updateUniformBlock();
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
