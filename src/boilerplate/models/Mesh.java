package boilerplate.models;

import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.buffers.VertexArray;
import boilerplate.rendering.buffers.VertexArrayBuffer;
import boilerplate.rendering.buffers.VertexElementBuffer;
import boilerplate.rendering.textures.Texture2d;
import boilerplate.utility.Logging;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Mesh {
    public static final int MAX_BONE_INFLUENCE = 4;

    public static abstract class Vertex implements Serializable {
        public abstract VertexArray.Layout getVertexArrayLayout();
    }

    public static class VertexDefault extends Vertex {
        public float p;
        public float p2;
//        public float[] position = new float[3];
//        public float[] normal = new float[3];
//        public float[] texCoords = new float[2];
//        public float[] tangent = new float[3];
//        public float[] bitangent = new float[3];
//        public int[] boneIds = new int[MAX_BONE_INFLUENCE];
//        public float[] boneWeights = new float[MAX_BONE_INFLUENCE];

        public VertexArray.Layout getVertexArrayLayout() {
            VertexArray.Layout l = new VertexArray.Layout();
            l.pushFloat(1);
//            l.pushFloat(3);
//            l.pushFloat(2);
//            l.pushFloat(3);
//            l.pushFloat(3);
//            l.pushInt(MAX_BONE_INFLUENCE);
//            l.pushFloat(MAX_BONE_INFLUENCE);
            return l;
        }
    }

    public boolean debugSetup = false;

    VertexArray va = new VertexArray();
    VertexArrayBuffer vb = new VertexArrayBuffer();
    VertexElementBuffer veb = new VertexElementBuffer(VertexElementBuffer.ELEMENT_TYPE_INT);

    Vertex[] vertices;
    int[] indices;
    Texture2d[] textures;

    public void setup(Vertex[] vertices, int[] indices, Texture2d[] textures) {
//        this.vertices = vertices;
//        this.indices = indices;
//        this.textures = textures;
//
//        va.genId();
//        vb.genId();
//        veb.genId();

//        va.bindBuffer(vb, veb);
//        va.pushLayout(vertices[0].getVertexArrayLayout());

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteStream)) {
            out.writeObject(0);
            out.flush();
//            vb.bufferData(byteStream.toByteArray());
        } catch (IOException e) {
            Logging.danger("Error attempting to serialize given vertices into byte array.");
            throw new RuntimeException(e);
        }
//        veb.bufferData(indices);

        if (debugSetup) debug(byteStream);
    }

    private void debug(ByteArrayOutputStream byteStream) {
        byte[] arr = byteStream.toByteArray();
        ByteBuffer buff = ByteBuffer.wrap(arr);

        Logging.mystical("ByteArrayOutputStream to byte[]; size: %s, data: %s", arr.length, Arrays.toString(arr));
        ByteArrayInputStream bis = new ByteArrayInputStream(arr);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Logging.mystical(Arrays.toString(bis.readAllBytes()));
    }

    public void draw(ShaderProgram shaderProgram) {

    }
}
