package boilerplate.example;

import boilerplate.common.BoilerplateConstants;
import boilerplate.common.GameBase;
import boilerplate.common.TimeStepper;
import boilerplate.common.Window;
import boilerplate.models.Model;
import boilerplate.rendering.Camera3d;
import boilerplate.rendering.Renderer;
import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.SkyBox;
import boilerplate.rendering.buffers.FrameBuffer;
import boilerplate.rendering.buffers.VertexArray;
import boilerplate.rendering.buffers.VertexArrayBuffer;
import boilerplate.rendering.builders.*;
import boilerplate.rendering.textures.CubeMap;
import boilerplate.rendering.textures.Texture2dMultisample;
import boilerplate.utility.Logging;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL45;

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
    ShaderProgram shReflect = new ShaderProgram();
    ShaderProgram shOutline = new ShaderProgram();
    VertexArray va = new VertexArray();
    VertexArrayBuffer vb = new VertexArrayBuffer();
    CubeMap ballerCube = new CubeMap();
    SkyBox skyBox = new SkyBox();

    FrameBuffer fb = new FrameBuffer(SCREEN_SIZE);

    ShaderProgram modelShader = new ShaderProgram();
    Model modelBackpack = new Model();

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
        ballerCube.loadFaces("res/textures/baller.png");
        ballerCube.useNearestInterpolation();
        ballerCube.useClampEdgeWrap();
        CubeMap.unbind();
        va.genId();
        vb.genId();

        sh.autoInitializeShadersMulti("shaders/3d.glsl");
        shReflect.autoInitializeShadersMulti("shaders/3d_reflect.glsl");
        shOutline.autoInitializeShadersMulti("shaders/3d_outline.glsl");

        camera.setupUniformBuffer(sh, shReflect, shOutline);
        skyBox.setupBuffers(camera, "res/textures/space_skybox", "png");

        va.fastSetup(new int[]{3, 3}, vb);
        BufferBuilder3f bb3 = new BufferBuilder3f(true, 3);
        Shape3d.Poly3d poly = Shape3d.createCube(new Vector3f(), 1);
        poly.mode = new ShapeMode.Unpack(Shape3d.defaultCubeNormals());
        bb3.pushPolygon(poly);
        vb.bufferData(bb3);

        finalSh.autoInitializeShadersMulti("shaders/3d_final.glsl");
        finalVa.genId();
        finalVb.genId();

        finalVa.fastSetup(new int[]{2}, finalVb);
        BufferBuilder2f bb2 = new BufferBuilder2f(true);
        Shape2d.Poly2d poly2 = Shape2d.createRect(new Vector2f(-1), new Vector2f(2));
        bb2.pushPolygon(poly2);
        finalVb.bufferData(bb2);

        fb.genId();
        fb.setupIntermediaryFB();
        Texture2dMultisample buff = new Texture2dMultisample(SCREEN_SIZE, true);
        buff.bind();
        buff.createTexture2d(FrameBuffer.defaultColourBuffFormat, 4);
        FrameBuffer.RenderBuffer rb = new FrameBuffer.RenderBuffer(true);
        rb.createBufferMultisample(SCREEN_SIZE, GL45.GL_DEPTH24_STENCIL8, GL45.GL_DEPTH_STENCIL_ATTACHMENT, 4);
        fb.attachColourBuffer(buff);
        fb.attachRenderBuffer(rb);
        fb.checkCompletionOrError();
        FrameBuffer.unbind();

        modelShader.autoInitializeShadersMulti("shaders/3d_model.glsl");
        camera.bindShaderToUniformBlock(modelShader);
        modelBackpack.loadModel("res/models/bloxy-cola/BloxyColaUpdated.obj");
//        Mesh.VertexDefault v1 = new Mesh.VertexDefault();
//        Mesh.VertexDefault v2 = new Mesh.VertexDefault();
//        v1.p = 0;
//        v1.position = new float[] {1};
//        v2.position = new float[] {2, 2, 2};
//        m.debugSetup = true;
//        m.setup(new Mesh.VertexDefault[] {v1}, new int[] {}, new Texture2d[] {});
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
        Renderer.enableDepthTest();
//        Renderer.enableStencilTest();
//        Renderer.setStencilFunc(GL_ALWAYS, 1, true);  // write 1 to all fragments that pass
//        Renderer.enableStencilWriting();
        Renderer.clearCDS();

//        sh.bind();
//        ballerCube.bind();
//        drawObjects(model1, model2, sh);
//
//        Renderer.setStencilFunc(GL_NOTEQUAL, 1, true);  // only draw if fragment in stencil is NOT equal to 1
//        Renderer.disableStencilWriting();
//        Renderer.cullFrontFace();
//        drawObjects(model1.scale(1.2f), model2.scale(1.2f), shOutline);
//        Renderer.cullBackFace();
//        Renderer.disableStencilTest();
//
        shReflect.bind();
        shReflect.uniform3f("camPos", camera.getPos());
        skyBox.bindSkyBoxTexture();
        drawObjects(model1.translate(2, 0, 0), model2.translate(2, 0, 0), shReflect);

        modelBackpack.draw(modelShader);

        skyBox.draw();

        fb.blitIntoIntermediaryFB(GL_COLOR_BUFFER_BIT, GL_NEAREST);

        // --- POST PROCESSING --- //
        FrameBuffer.unbind();
        Renderer.clearC();
        Renderer.disableDepthTest();

        finalSh.bind();
        fb.bindIntermediaryFBColorBuffer();
        Renderer.drawArrays(GL_TRIANGLE_STRIP, finalVa, 4);

        Renderer.finish(window);
    }

    private void drawObjects(Matrix4f model1, Matrix4f model2, ShaderProgram sh) {
        sh.uniformMatrix4f("model", model1);
        Renderer.drawArrays(renderWireFrame ? GL_LINES : GL_TRIANGLES, va, 36);

        sh.uniformMatrix4f("model", model2);
        Renderer.drawArrays(renderWireFrame ? GL_LINES : GL_TRIANGLES, va, 36);
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
