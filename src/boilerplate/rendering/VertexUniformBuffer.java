package boilerplate.rendering;

import boilerplate.utility.Logging;
import org.lwjgl.opengl.GL45;

public class VertexUniformBuffer extends VertexBuffer {
    public VertexUniformBuffer(){
        this.bufferType = GL45.GL_UNIFORM_BUFFER;
    }

    public VertexUniformBuffer(boolean genId) {
        this();
        if (genId) genId();
    }

    // TODO

    @Override
    public void setBufferType(int bufferType) {
        Logging.danger("Cannot set buffer type on this object. Use a VertexBuffer instead.");
    }
}
