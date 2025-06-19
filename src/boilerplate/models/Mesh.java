package boilerplate.models;

import boilerplate.rendering.Renderer;
import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.buffers.VertexArray;
import boilerplate.rendering.buffers.VertexArrayBuffer;
import boilerplate.rendering.buffers.VertexElementBuffer;
import boilerplate.rendering.buffers.VertexLayout;
import boilerplate.rendering.textures.Texture2d;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIVector2D;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.opengl.GL45;

import java.awt.desktop.ScreenSleepEvent;
import java.nio.ByteBuffer;

public class Mesh {
    public static final int MAX_BONE_INFLUENCE = 4;

//    public static class Vertex implements Serializable {
//        public static final int TYPE_POSITION = 1;
//        public static final int TYPE_NORMAL = 2;
//        public static final int TYPE_TEX_POS = 3;
//
//        public static final Map<Integer, String> typeStrings = Map.of(TYPE_POSITION, "position", TYPE_NORMAL, "normal", TYPE_TEX_POS, "tex_pos");
//
//        public int[] layoutTypes;
//        public int[] layoutCount;
//
//        public Vertex(int[] layoutTypes, int[] layoutCount) {
//            if (layoutTypes.length != layoutCount.length) {
//                throw new RuntimeException("Vertex layout not valid, type and count arrays are expected wo be of equal length. types length: %s, counts length: %s".formatted(layoutTypes.length, layoutCount.length));
//            }
//
//            this.layoutTypes = layoutTypes;
//            this.layoutCount = layoutCount;
//        }
//
//        public static Vertex defaultVertex() {
//            return new Vertex(
//                    new int[]{TYPE_POSITION, TYPE_POSITION, TYPE_NORMAL},
//                    new int[]{3, 3, 2}
//            );
//        }
//
//        @Override
//        public String toString() {
//            StringBuilder s = new StringBuilder().append("Vertex(");
//            for (int i = 0; i < layoutTypes.length; i++) {
//                if (i != 0) s.append(", ");
//                s.append("%s%s".formatted(typeStrings.get(layoutTypes[i]), layoutCount[i]));
//            }
//            return s.append(")").toString();
//        }
//    }

    VertexArray va = new VertexArray();
    VertexArrayBuffer vb = new VertexArrayBuffer();
    VertexElementBuffer veb = new VertexElementBuffer(VertexElementBuffer.ELEMENT_TYPE_INT);

    VertexLayout vertexLayout;
//    Vertex[] vertices;
//    int[] indices;
//    Texture2d[] textures;

    int vertexCount = 0;
    ByteBuffer data = ByteBuffer.allocate(0);
    ByteBuffer indices = ByteBuffer.allocate(0);

    public Mesh(VertexLayout vertexLayout) {
        this.vertexLayout = vertexLayout;
    }

    public void allocateMemory(int verticesBytes, int indicesBytes) {
        System.out.println(verticesBytes);
        data = ByteBuffer.allocate(verticesBytes);
        indices = ByteBuffer.allocate(indicesBytes);
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

//    public void setup(Vertex[] vertices, int[] indices, Texture2d[] textures) {
////        this.vertices = vertices;
////        this.indices = indices;
////        this.textures = textures;
////
////        va.genId();
////        vb.genId();
////        veb.genId();
//
////        va.bindBuffer(vb, veb);
////        va.pushLayout(vertices[0].getVertexArrayLayout());
//
//        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//        try (ObjectOutputStream out = new ObjectOutputStream(byteStream)) {
//            out.writeObject(0);
//            out.flush();
////            vb.bufferData(byteStream.toByteArray());
//        } catch (IOException e) {
//            Logging.danger("Error attempting to serialize given vertices into byte array.");
//            throw new RuntimeException(e);
//        }

    /// /        veb.bufferData(indices);
//    }
    public void draw(ShaderProgram shaderProgram) {
        Renderer.drawElements(GL45.GL_TRIANGLES, va, veb, vertexCount);
    }
}
