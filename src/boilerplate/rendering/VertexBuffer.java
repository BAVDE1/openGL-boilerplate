package boilerplate.rendering;

import boilerplate.rendering.builders.BufferBuilder2f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;
import boilerplate.utility.Logging;

import static org.lwjgl.opengl.GL45.*;

public class VertexBuffer {
    protected int bufferType = GL45.GL_ARRAY_BUFFER;
    protected int drawMethod = GL15.GL_DYNAMIC_DRAW;
    private Integer bufferId;

    public VertexBuffer(){}
    public VertexBuffer(boolean genId) {if (genId) genId();}

    public void setDrawMethod(int newDrawMethod) {
        this.drawMethod = newDrawMethod;
    }

    public void bufferData(float[] data) {
        Renderer.bindBuffer(this);
        GL45.glBufferData(bufferType, data, drawMethod);
    }

    public void bufferData(int[] data) {
        Renderer.bindBuffer(this);
        GL45.glBufferData(bufferType, data, drawMethod);
    }

    public void bufferData(BufferBuilder2f bb) {
        bufferData(bb.getFloats());
    }

    public void bufferSize(int size) {
        Renderer.bindBuffer(this);
        GL45.glBufferData(bufferType, size, drawMethod);
    }

    public void bufferSubData(float[] data) {
        bufferSubData(0, data);
    }

    public void bufferSubData(int bytesOffset, float[] data) {
        Renderer.bindBuffer(this);
        GL45.glBufferSubData(bufferType, bytesOffset, data);
    }

    public void genId() {
        if (bufferId != null) {
            Logging.warn("Attempting to re-generate already generated buffer id, aborting");
            return;
        }
        bufferId = GL45.glGenBuffers();
    }

    public void delete() {
        glDeleteBuffers(bufferId);
    }

    public int getId() {return bufferId;}
    public int getBufferType() {return bufferType;}
    public int getDrawMethod() {return drawMethod;}
}
