package src.rendering;

import src.game.Window;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    public static void clearScreen() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public static void draw(int mode, VaoHelper vao) {

    }

    public static void finish(Window window) {
        glfwSwapBuffers(window.handle);
    }
}
