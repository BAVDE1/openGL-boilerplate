package boilerplate.rendering;

import boilerplate.rendering.buffers.VertexArray;
import boilerplate.rendering.buffers.VertexElementBuffer;
import org.lwjgl.opengl.GL;
import boilerplate.common.Window;
import boilerplate.rendering.text.TextRenderer;
import boilerplate.utility.Logging;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL45.*;

/**
 * State Machine
 * Automatically binds and draws VertexArrays & Shaders
 */
public class Renderer {
    /** Do before anything GL related */
    public static void setupGLContext(boolean setupDefaults) {
        GL.createCapabilities();

        if (setupDefaults) {
            enableDebugOutput();
            enableTexture2d();
            applyDefaultBlend();
            setClearColour(0, 0, 0, 0);
        }

        Logging.debug("OpenGL capabilities created");
    }

    public static void enableDebugOutput() {
        glEnable(GL_DEBUG_OUTPUT);
    }

    public static void enableTexture2d() {
        glEnable(GL_TEXTURE_2D);
    }

    public static void enableDepthTest() {
        glEnable(GL_DEPTH_TEST);
    }

    public static void enableStencilTest() {
        glEnable(GL_STENCIL_TEST);
    }

    public static void setStencilOperation(int stencilFails, int stencilPassDepthFails, int stencilPassDepthPass) {
        glStencilOp(stencilFails, stencilPassDepthFails, stencilPassDepthPass);
    }

    public static void enableFaceCulling(int windingOrder, int faceToCull) {
        glEnable(GL_CULL_FACE);
        glCullFace(faceToCull);
        glFrontFace(windingOrder);
    }

    public static void enableFaceCullingDefault() {
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);  // note: winding order is calculated after vertex shader
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
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    public static void setWindingOrder(int order) {
        glFrontFace(order);
    }

    public static void enableFaceCulling(int face) {
        glEnable(GL_CULL_FACE);
        glCullFace(face);
    }

    public static void drawArrays(int mode, VertexArray va, int vertexCount) {
        va.bind();
        glDrawArrays(mode, 0, vertexCount);
    }

    public static void drawInstanced(int mode, VertexArray va, int vertsPerInstance, int instanceCount) {
        va.bind();
        glDrawArraysInstanced(mode, 0, vertsPerInstance, instanceCount);
    }

    public static void drawElements(int mode, VertexArray va, VertexElementBuffer veb, int vertexCount) {
        drawElements(mode, va, veb.getElementType(), vertexCount);
    }

    public static void drawElements(int mode, VertexArray va, int elementType, int vertexCount) {
        va.bind();
        glDrawElements(mode, vertexCount, elementType, 0);
    }

    public static void drawText(TextRenderer tr) {
        tr.draw();
    }

    public static void finish(Window window) {
        glfwSwapBuffers(window.handle);
    }
}
