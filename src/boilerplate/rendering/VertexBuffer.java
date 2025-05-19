package boilerplate.rendering;

import boilerplate.rendering.builders.BufferBuilder;
import boilerplate.rendering.builders.BufferBuilder2f;
import boilerplate.utility.MathUtils;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;
import boilerplate.utility.Logging;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL45.*;

public class VertexBuffer {
    protected int bufferType = GL45.GL_ARRAY_BUFFER;
    protected int drawMethod = GL15.GL_DYNAMIC_DRAW;
    protected Integer bufferId;

    public VertexBuffer(){}
    public VertexBuffer(boolean genId) {if (genId) genId();}

    public void setBufferType(int bufferType) {
        this.bufferType = bufferType;
    }

    public void setDrawMethod(int newDrawMethod) {
        this.drawMethod = newDrawMethod;
    }

    public void bind() {
        glBindBuffer(getBufferType(), bufferId);
    }

    public void unbind() {
        glBindBuffer(getBufferType(), 0);
    }

    public void bufferData(float[] data) {
        bind();
        GL45.glBufferData(bufferType, data, drawMethod);
    }

    public void bufferData(FloatBuffer data) {
        bind();
        GL45.glBufferData(bufferType, data, drawMethod);
    }

    public void bufferData(int[] data) {
        bind();
        GL45.glBufferData(bufferType, data, drawMethod);
    }

    public void bufferData(BufferBuilder bb) {
        bufferData(bb.getFloats());
    }

    public void bufferSize(int bytesSize) {
        bind();
        GL45.glBufferData(bufferType, bytesSize, drawMethod);
    }

    public void bufferSubData(int bytesOffset, float[] data) {
        bind();
        GL45.glBufferSubData(bufferType, bytesOffset, data);
    }

    public void bufferSubData(int bytesOffset, FloatBuffer data) {
        bind();
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
