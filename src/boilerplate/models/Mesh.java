package boilerplate.models;

import boilerplate.rendering.buffers.VertexArray;
import boilerplate.rendering.buffers.VertexArrayBuffer;
import boilerplate.rendering.buffers.VertexElementBuffer;
import boilerplate.rendering.textures.Texture2d;

import java.beans.JavaBean;
import java.io.Serializable;
import java.lang.reflect.Type;

public class Mesh {
    public static final int MAX_BONE_INFLUENCE = 4;

    public static abstract class Vertex implements Serializable {
        public static class VertexDefault extends Vertex {
            float[] position = new float[3];
            float[] normal = new float[3];
            float[] texCoords = new float[2];
            float[] tangent = new float[3];
            float[] bitangent = new float[3];
            int[] boneIds = new int[MAX_BONE_INFLUENCE];
            float[] boneWeights = new float[MAX_BONE_INFLUENCE];
        }
    }

    VertexArray va = new VertexArray();
    VertexArrayBuffer vb = new VertexArrayBuffer();
    VertexElementBuffer veb = new VertexElementBuffer(VertexElementBuffer.ELEMENT_TYPE_INT);

    Vertex[] vertices;
    int[] indices;
    Texture2d[] textures;

    public Mesh() {

    }

    protected void setup() {

    }

    public void draw() {

    }
}
