package src.rendering;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;
import src.utility.Logging;

public class VertexBuffer {
    private final int bufferType = GL45.GL_ARRAY_BUFFER;
    private final int drawMethod = GL15.GL_DYNAMIC_DRAW;
    private Integer bufferId;

    public VertexBuffer(){}

    public void bufferData(float[] data) {
        Renderer.bindBuffer(this);
        GL45.glBufferData(bufferType, data, drawMethod);
    }

    public void bufferSize(int size) {
        Renderer.bindBuffer(this);
        GL45.glBufferData(bufferType, size, drawMethod);
    }

    public void BufferSubData(float[] data) {
        BufferSubData(0, data);
    }

    public void BufferSubData(int bytesOffset, float[] data) {
        Renderer.bindBuffer(this);
        GL45.glBufferSubData(GL15.GL_ARRAY_BUFFER, bytesOffset, data);
    }

    public void genId() {
        if (bufferId != null) {
            Logging.warn("Attempting to re-generate already generated buffer id, aborting");
            return;
        }
        bufferId = GL45.glGenBuffers();
    }

    public int getId() {return bufferId;}
    public int getBufferType() {return bufferType;}
    public int getDrawMethod() {return drawMethod;}
}
