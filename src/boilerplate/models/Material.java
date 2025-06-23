package boilerplate.models;

import boilerplate.rendering.textures.Texture2d;
import org.joml.Vector4f;

public class Material {
    public static final Vector4f DEFAULT_COLOUR = new Vector4f(0, 0, 0, 1);

    public Vector4f ambient;
    public Vector4f diffuse;
    public Vector4f specular;

    public Texture2d texture;

    public Material() {
    }

    public Material(Vector4f ambient, Vector4f diffuse, Vector4f specular) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
    }

    public Material(Vector4f ambient, Vector4f diffuse, Vector4f specular, Texture2d texture) {
        this(ambient, diffuse, specular);
        this.texture = texture;
    }

    public void bindTexture() {
        if (texture == null) return;
        texture.bind();
    }

    @Override
    public String toString() {
        return "Material(amb: %s, dif: %s, spec: %s, %s)".formatted(ambient, diffuse, specular, texture);
    }
}
