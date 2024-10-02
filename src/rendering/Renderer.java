package src.rendering;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL45;
import src.game.Window;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    private static int boundArray = 0;
    private static int boundBuffer = 0;

    /** Do before anything GL related */
    public static void setupGLContext() {
        GL.createCapabilities();

        glEnable(GL45.GL_DEBUG_OUTPUT);
        glEnable(GL_TEXTURE_2D);

        // transparency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glClearColor(.0f, .0f, .0f, .0f);
    }

    public static void clearScreen() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public static void draw(int mode, VertexArray va, int count) {
        Renderer.bindArray(va);
        glDrawArrays(mode, 0, count);
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
        GL45.glBindVertexArray(id);
    }

    public static void bindBuffer(VertexBuffer vb) {
        bindBuffer(vb.getBufferType(), vb.getId());
    }
    public static void bindBuffer(int bufferType, int id) {
        if (id == boundBuffer) return;
        boundBuffer = id;
        GL45.glBindBuffer(bufferType, id);
    }
}
