package boilerplate.rendering;

import org.lwjgl.opengl.GL;
import boilerplate.common.Window;
import boilerplate.rendering.text.TextRenderer;
import boilerplate.utility.Logging;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL45.*;

/**
 * State Machine
 * Automatically binds and draws VertexArrays & Shaders
 */
public class Renderer {
    private static int boundArray = 0;
    private static int boundBuffer = 0;
    private static int boundShader = 0;

    /** Do before anything GL related */
    public static void setupGLContext() {
        GL.createCapabilities();

        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_TEXTURE_2D);

        applyDefaultBlend();

        setClearColour(0, 0, 0, 0);
        Logging.debug("OpenGL capabilities created");
    }

    public static void enableDepthTest() {
        glEnable(GL_DEPTH_TEST);
    }

    public static void enableFaceCulling(int windingOrder, int faceToCull) {
        glEnable(GL_CULL_FACE);
        glCullFace(faceToCull);
        glFrontFace(windingOrder);
    }

    public static void enableFaceCullingDefault() {
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);
    }

    /** logically behaving transparency */
    public static void applyDefaultBlend() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void setClearColour(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
    }

    public static void clearScreen() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public static void setWindingOrder(int order) {
        glFrontFace(order);
    }

    public static void enableFaceCulling(int face) {
        glEnable(GL_CULL_FACE);
        glCullFace(face);
    }

    public static void drawArrays(int mode, VertexArray va, int vertexCount) {
        bindArray(va);
        glDrawArrays(mode, 0, vertexCount);
    }

    public static void drawInstanced(int mode, VertexArray va, int vertsPerInstance, int instanceCount) {
        bindArray(va);
        glDrawArraysInstanced(mode, 0, vertsPerInstance, instanceCount);
    }

    public static void drawElements(int mode, VertexArray va, VertexElementBuffer veb, int vertexCount) {
        drawElements(mode, va, veb.getElementType(), vertexCount);
    }

    public static void drawElements(int mode, VertexArray va, int elementType, int vertexCount) {
        bindArray(va);
        glDrawElements(mode, vertexCount, elementType, 0);
    }

    public static void drawText(TextRenderer tr) {
        tr.draw();
    }

    public static void finish(Window window) {
        glfwSwapBuffers(window.handle);
    }

    public static void bindArray(VertexArray va) {
        int id = va.getId();
        boundArray = id;
        glBindVertexArray(id);
    }
    public static void unBindArray() {
        boundArray = 0;
        glBindVertexArray(0);
    }

    public static void bindBuffer(VertexBuffer vb) {
        int id = vb.getId();
        boundBuffer = id;
        glBindBuffer(vb.getBufferType(), id);
    }
    public static void unBindBuffer() {
        boundBuffer = 0;
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public static void bindShader(ShaderHelper sh) {
        if (sh.getProgram() == boundShader) return;
        boundShader = sh.getProgram();
        glUseProgram(sh.getProgram());
    }
    public static void unBindShader() {
        boundShader = 0;
        glUseProgram(0);
    }

    public static void unbindAll() {
        unBindShader();
        unBindBuffer();
        unBindArray();
    }
}
