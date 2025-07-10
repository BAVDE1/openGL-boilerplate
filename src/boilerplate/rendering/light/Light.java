package boilerplate.rendering.light;

import boilerplate.rendering.ShaderProgram;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class Light {
    public static Vector3f DEFAULT_COLOUR = new Vector3f(1);

    public static class LightGroup {
        public ArrayList<Light> lights = new ArrayList<>();

        public void addLight(Light... light) {
            lights.addAll(List.of(light));
        }

        public void uniformValuesAsArray(String uniform, ShaderProgram sh) {
            for (int i = 0; i < lights.size(); i++) {
                String indexedUniform = "%s[%s]".formatted(uniform, i);
                lights.get(i).uniformValues(indexedUniform, sh);
            }
        }
    }

    public Vector3f ambient = DEFAULT_COLOUR.mul(.2f, new Vector3f());
    public Vector3f diffuse = DEFAULT_COLOUR;
    public Vector3f specular = DEFAULT_COLOUR;

    public void setColourValues(Vector3f diffuse, Vector3f specular, Vector3f ambient) {
        this.diffuse = diffuse;
        this.specular = specular;
        this.ambient = ambient;
    }

    public void uniformValues(String uniform, ShaderProgram sh) {
        sh.uniform3f(uniform + ".ambient", ambient);
        sh.uniform3f(uniform + ".diffuse", diffuse);
        sh.uniform3f(uniform + ".specular", specular);
    }
}
