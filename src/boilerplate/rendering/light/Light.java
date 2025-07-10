package boilerplate.rendering.light;

import boilerplate.rendering.ShaderProgram;
import org.joml.Vector3f;

import java.util.ArrayList;

public abstract class Light {
    public static Vector3f DEFAULT_COLOUR = new Vector3f(1);

    public static class LightGroup {
        public ArrayList<PointLight> pointLights;
        public ArrayList<DirectionalLight> directionalLights;
        public ArrayList<SpotLight> spotLights;
        public ArrayList<Light> otherLights;
    }

    public Vector3f ambient = DEFAULT_COLOUR.mul(.2f, new Vector3f());
    public Vector3f diffuse = DEFAULT_COLOUR;
    public Vector3f specular = DEFAULT_COLOUR;

    public void uniformValues(String uniform, ShaderProgram sh) {
        sh.uniform3f(uniform + ".ambient", ambient);
        sh.uniform3f(uniform + ".diffuse", diffuse);
        sh.uniform3f(uniform + ".specular", specular);
    };
}
