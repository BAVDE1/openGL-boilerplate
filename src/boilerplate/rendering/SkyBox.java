package boilerplate.rendering;

import boilerplate.rendering.buffers.VertexArray;
import boilerplate.rendering.buffers.VertexArrayBuffer;
import boilerplate.rendering.buffers.VertexElementBuffer;
import boilerplate.rendering.builders.BufferBuilder3f;
import boilerplate.rendering.builders.Shape3d;
import boilerplate.rendering.textures.CubeMap;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL45;

import static org.lwjgl.opengl.GL11.*;

public class SkyBox {
    public static String[] expectedImageNames = new String[]{"px", "nx", "py", "ny", "pz", "nz"};
    public static String ShaderCodeVertex = """
            #version 450 core
            
            layout(location = 0) in vec3 pos;
            
            layout (std140) uniform %s {
                mat4 projection;
                mat4 view;
            };
            
            out vec3 v_texPos;
            
            void main() {
                gl_Position = (projection * mat4(mat3(view)) * vec4(pos, 1)).xyww;  // z values are always maximum (1.0) (w / w = 1.0)
                v_texPos = pos;
            }
            """;

    public static String ShaderCodeFragment = """
            #version 450 core
            
            uniform samplerCube skyBoxTexture;
            
            in vec3 v_texPos;
            
            out vec4 colour;
            
            void main() {
                colour = vec4(texture(skyBoxTexture, v_texPos).xyz, 1);
            }
            """;

    protected ShaderProgram sh = new ShaderProgram();
    protected VertexArray va = new VertexArray();
    protected VertexArrayBuffer vb = new VertexArrayBuffer();
    protected VertexElementBuffer veb = new VertexElementBuffer(VertexElementBuffer.ELEMENT_TYPE_INT);
    protected CubeMap skyBoxTexture = new CubeMap();

    protected Camera3d camera3d;

    public SkyBox() {

    }

    public void setupBuffers(Camera3d camera3d, String texturesDirectory, String imageExtension) {
        this.camera3d = camera3d;

        sh.genProgram();
        sh.attachShader(String.format(ShaderCodeVertex, camera3d.uniformBlockName), GL45.GL_VERTEX_SHADER, "SkyBox class");
        sh.attachShader(ShaderCodeFragment, GL45.GL_FRAGMENT_SHADER, "SkyBox class");
        sh.linkProgram();
        camera3d.bindShaderToUniformBlock(sh);

        va.genId();
        vb.genId();
        veb.genId();
        va.fastSetup(new int[]{3}, vb, veb);

        BufferBuilder3f bb = new BufferBuilder3f(true);
        Shape3d.Poly3d poly = Shape3d.createCubeE(new Vector3f(), 1);
        bb.pushPolygon(poly);
        vb.bufferData(bb);
        veb.bufferData(poly.elementIndex);

        String[] textureFileNames = new String[6];
        for (int i = 0; i < 6; i++) {
            textureFileNames[i] = String.format("%s/%s.%s", texturesDirectory, expectedImageNames[i], imageExtension);
        }
        skyBoxTexture.genId();
        skyBoxTexture.loadFaces(textureFileNames);
        skyBoxTexture.useLinearInterpolation();
        skyBoxTexture.useClampEdgeWrap();
        CubeMap.unbind();
    }

    public void bindSkyBoxTexture() {
        skyBoxTexture.bind();
    }

    public void draw() {
        sh.bind();
        bindSkyBoxTexture();

        GL45.glDepthFunc(GL_LEQUAL);  // since all skybox depth (z) values are exactly maximum (1.0)
        Renderer.cullFrontFace();
        Renderer.drawElements(GL_TRIANGLES, va, veb, 36);
        Renderer.cullBackFace();
        GL45.glDepthFunc(GL_LESS);
    }
}
