package src.rendering;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;

public class BufferHelper {
    private int bufferType
    public int bufferId;

    public BufferHelper(){}

    public void genId() {
        bufferId = GL45.glGenBuffers();
    }

    public void bind() {
        GL45.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
    }

    public void unbind() {
        GL45.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
}
