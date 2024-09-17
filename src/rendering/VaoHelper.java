package src.rendering;

import org.lwjgl.opengl.GL45;

public class VaoHelper {
    public int vaoId;

    public VaoHelper(){}

    public void genId() {
        vaoId = GL45.glGenVertexArrays();
    }

    public void bind() {
        GL45.glBindVertexArray(vaoId);
    }

    public void unbind() {
        GL45.glBindVertexArray(0);
    }
}
