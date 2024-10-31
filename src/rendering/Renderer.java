package src.rendering;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL45;
import src.game.Window;
import src.rendering.text.TextRenderer;
import src.utility.Logging;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL45.*;

public class Renderer {
    private static int boundArray = 0;
    private static int boundBuffer = 0;

    /** Do before anything GL related */
    public static void setupGLContext() {
        GL.createCapabilities();

        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_TEXTURE_2D);

        // transparency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glClearColor(.0f, .0f, .0f, .0f);
        Logging.debug("OpenGL capabilities created");
    }

    public static void clearScreen() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public static void draw(int mode, VertexArray va, int count) {
        Renderer.bindArray(va);
        glDrawArrays(mode, 0, count);
    }

    public static void draw(TextRenderer tr) {
        tr.draw();
    }

    public static void drawInstanced(int mode, VertexArray va, int vertsPerInstance, int instanceCount) {
        Renderer.bindArray(va);
        glDrawArraysInstanced(mode, 0, vertsPerInstance, instanceCount);
    }

    public static void finish(Window window) {
        glfwSwapBuffers(window.handle);
    }

    public static void bindArray(VertexArray va) {
        bindArray(va.getId());
    }
    public static void bindArray(int id) {
        if (id == boundArray) return;
        boundArray = id;
        glBindVertexArray(id);
    }
    public static void unBindArray() {
        boundArray = 0;
        glBindVertexArray(0);
    }

    public static void bindBuffer(VertexBuffer vb) {
        bindBuffer(vb.getBufferType(), vb.getId());
    }
    public static void bindBuffer(int bufferType, int id) {
        if (id == boundBuffer) return;
        boundBuffer = id;
        glBindBuffer(bufferType, id);
    }
    public static void unBindBuffer() {
        boundArray = 0;
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
