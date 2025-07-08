package boilerplate.models;

import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.textures.Texture2d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Material {
    public static final Vector3f DEFAULT_COLOUR = new Vector3f(0);

    public String uniformStructName = "material";
    public Vector3f ambient = DEFAULT_COLOUR;
    public Vector3f diffuse = DEFAULT_COLOUR;
    public Vector3f specular = new Vector3f(.5f);
    public float shininess = 32;

    public Texture2d texture;

    public Material() {
    }

    public Material(Vector3f colour) {
        this.ambient = colour;
        this.diffuse = colour;
    }

    public Material(Vector3f colour, float shininess) {
        this(colour);
        this.shininess = shininess;
    }

    public Material(Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
    }

    public Material(Vector3f ambient, Vector3f diffuse, Vector3f specular, Texture2d texture) {
        this(ambient, diffuse, specular);
        this.texture = texture;
    }

    public void bindTexture() {
        if (texture == null) return;
        texture.bind();
    }

    public void uniformValues(ShaderProgram sh) {
        sh.uniform3f(uniformStructName + ".ambient", ambient);
        sh.uniform3f(uniformStructName + ".diffuse", diffuse);
        sh.uniform3f(uniformStructName + ".specular", specular);
        sh.uniform1f(uniformStructName + ".shininess", shininess);
    }

    @Override
    public String toString() {
        return "Material(amb: %s, dif: %s, spec: %s, %s)".formatted(ambient, diffuse, specular, texture);
    }
}
