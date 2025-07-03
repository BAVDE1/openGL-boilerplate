package boilerplate.common;

public class BoilerplateShaders {
    public static String safeFormat(String shaderCode, String ignore, Object... args) {
        String safeCode = shaderCode.replaceAll(ignore, "~~~~");
        safeCode = String.format(safeCode, args);
        return safeCode.replaceAll("~~~~", ignore);
    }

    public static String SkyBoxVertex = """
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

    public static String SkyBoxFragment = """
            #version 450 core
            
            uniform samplerCube skyBoxTexture;
            
            in vec3 v_texPos;
            
            out vec4 colour;
            
            void main() {
                colour = vec4(texture(skyBoxTexture, v_texPos).xyz, 1);
            }
            """;

    public static String ModelBoneVertex = """
            #version 450 core
            
            layout(location = 0) in int boneId;
            
            layout (std140) uniform %s {
                mat4 projection;
                mat4 view;
            };
            
            const int MAX_BONES = 100;
            
            uniform mat4 model;
            uniform mat4 finalBonesMatrices[MAX_BONES];
            
            void main() {
                vec4 pos = finalBonesMatrices[boneId] * vec4(0, 0, 0, 1);
                gl_Position = projection * view * model * pos;
            }
            """;

    public static String ModelBoneFragment = """
            #version 450 core
            
            out vec4 colour;
            
            void main() {
                colour = vec4(1, 0, 0, 1);
            }
            """;
}
