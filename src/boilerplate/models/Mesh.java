package boilerplate.models;

import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.buffers.VertexArray;
import boilerplate.rendering.buffers.VertexArrayBuffer;
import boilerplate.rendering.buffers.VertexElementBuffer;
import boilerplate.rendering.buffers.VertexLayout;
import boilerplate.rendering.textures.Texture2d;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIVector3D;

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

    public boolean debugSetup = false;

    VertexArray va = new VertexArray();
    VertexArrayBuffer vb = new VertexArrayBuffer();
    VertexElementBuffer veb = new VertexElementBuffer(VertexElementBuffer.ELEMENT_TYPE_INT);

    VertexLayout vertexLayout;
//    Vertex[] vertices;
//    int[] indices;
    Texture2d[] textures;

    float[] data;

    public Mesh(VertexLayout vertexLayout) {
        this.vertexLayout = vertexLayout;
    }

    public void pushPosition(AIVector3D position) {

    }

    public void pushNormal(AIVector3D normal) {

    }

    public void pushNormal(int x, int y, int z) {

    }

    public void pushTexPos(PointerBuffer texPosBuff) {

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
////        veb.bufferData(indices);
//    }

    public void draw(ShaderProgram shaderProgram) {

    }
}
