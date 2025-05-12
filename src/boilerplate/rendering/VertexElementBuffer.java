package boilerplate.rendering;

import boilerplate.utility.Logging;
import org.lwjgl.opengl.GL45;

import static org.lwjgl.opengl.GL45.glDeleteBuffers;

public class VertexElementBuffer extends VertexBuffer {
    public static final int ELEMENT_TYPE_BYTE = GL45.GL_UNSIGNED_BYTE;
    public static final int ELEMENT_TYPE_SHORT = GL45.GL_UNSIGNED_SHORT;
    public static final int ELEMENT_TYPE_INT = GL45.GL_UNSIGNED_INT;

    private final int elementType;

    public VertexElementBuffer(int elementType){
        this.bufferType = GL45.GL_ELEMENT_ARRAY_BUFFER;
        this.elementType = elementType;
    }

    public VertexElementBuffer(int elementType, boolean genId) {
        this(elementType);
        if (genId) genId();
    }

    public int getElementType() {
        return elementType;
    }

    @Override
    public void setBufferType(int bufferType) {
        Logging.danger("Cannot set buffer type on this object. Use a VertexBuffer instead.");
    }
}
