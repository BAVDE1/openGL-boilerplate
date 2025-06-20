package boilerplate.models;

import boilerplate.rendering.Renderer;
import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.buffers.VertexArray;
import boilerplate.rendering.buffers.VertexArrayBuffer;
import boilerplate.rendering.buffers.VertexElementBuffer;
import boilerplate.rendering.buffers.VertexLayout;
import org.lwjgl.assimp.AIVector2D;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class Mesh {
    public static final int MAX_BONE_INFLUENCE = 4;

    VertexArray va = new VertexArray();
    VertexArrayBuffer vb = new VertexArrayBuffer();
    VertexElementBuffer veb = new VertexElementBuffer(VertexElementBuffer.ELEMENT_TYPE_INT);

    VertexLayout vertexLayout;

    int vertexCount = 0;
    ByteBuffer data = MemoryUtil.memAlloc(0);
    ByteBuffer indices = MemoryUtil.memAlloc(0);
//    Texture2d[] textures;

    public Mesh(VertexLayout vertexLayout) {
        this.vertexLayout = vertexLayout;
    }

    public void allocateMemory(int verticesBytes, int indicesBytes) {
        data = MemoryUtil.memAlloc(verticesBytes);
        indices = MemoryUtil.memAlloc(indicesBytes);
    }

    public void pushIndice(int i) {
        indices.putInt(i);
    }

    public void pushFloats(float x, float y) {
        data.putFloat(x);
        data.putFloat(y);
    }

    public void pushFloats(float x, float y, float z) {
        data.putFloat(x);
        data.putFloat(y);
        data.putFloat(z);
    }

    public void pushVector2D(AIVector2D vector) {
        pushFloats(vector.x(), vector.y());
    }

    public void pushVector2D(AIVector3D vector) {
        pushFloats(vector.x(), vector.y());
    }

    public void pushVector3D(AIVector3D vector) {
        pushFloats(vector.x(), vector.y(), vector.z());
    }

    public void finalizeMesh() {
        va.genId();
        vb.genId();
        veb.genId();

        va.bindBuffer(vb, veb);
        va.pushLayout(vertexLayout);

        vb.bufferData(data);
        veb.bufferData(indices);
    }

    public void draw() {
        Renderer.drawElements(GL45.GL_LINES, va, veb, vertexCount);
    }
}
