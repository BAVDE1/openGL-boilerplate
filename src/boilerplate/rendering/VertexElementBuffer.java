package boilerplate.rendering;

import org.lwjgl.opengl.GL45;

import static org.lwjgl.opengl.GL45.glDeleteBuffers;

public class VertexElementBuffer extends VertexBuffer {
    public static final int TYPE_BYTE = GL45.GL_UNSIGNED_BYTE;
    public static final int TYPE_SHORT = GL45.GL_UNSIGNED_SHORT;
    public static final int TYPE_INT = GL45.GL_UNSIGNED_INT;

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
}
