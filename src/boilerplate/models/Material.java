package boilerplate.models;

import boilerplate.rendering.ShaderProgram;
import boilerplate.rendering.textures.Texture2d;
import org.joml.Vector3f;

public class Material {
    public static final Vector3f DEFAULT_COLOUR = new Vector3f(0);

    public String uniformStructName = "material";
    public Vector3f ambient = DEFAULT_COLOUR;
    public Vector3f diffuse = DEFAULT_COLOUR;
    public Vector3f specular = new Vector3f(1);
    public float shininess = 64;

    public Texture2d diffuseTexture;
    public Texture2d specularMap;

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

    public Material(Texture2d diffuseTexture) {
        this.diffuseTexture = diffuseTexture;
    }

    public Material(Texture2d diffuseTexture, Texture2d specularMap) {
        this.diffuseTexture = diffuseTexture;
        this.specularMap = specularMap;
    }

    public Material(Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
    }

    public Material(Vector3f ambient, Vector3f diffuse, Vector3f specular, Texture2d diffuseTexture) {
        this(ambient, diffuse, specular);
        this.diffuseTexture = diffuseTexture;
    }

    public void uniformValues(ShaderProgram sh) {
        sh.uniform3f(uniformStructName + ".ambient", ambient);
        sh.uniform3f(uniformStructName + ".diffuse", diffuse);
        sh.uniform3f(uniformStructName + ".specular", specular);
        sh.uniform1f(uniformStructName + ".shininess", shininess);
    }

    public void uniformAndBindTextures(ShaderProgram sh) {
        int texSlot = 0;
        if (diffuseTexture != null) {
            sh.uniformTexture(uniformStructName + ".diffuseTexture", diffuseTexture, texSlot++);
        }
        if (specularMap != null) {
            sh.uniformTexture(uniformStructName + ".specularMap", specularMap, texSlot++);
        }
    }

    @Override
    public String toString() {
        return "Material(amb: %s, dif: %s, spec: %s, %s)".formatted(ambient, diffuse, specular, diffuseTexture);
    }
}
