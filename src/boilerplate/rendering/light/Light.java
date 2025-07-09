package boilerplate.rendering.light;

import boilerplate.rendering.ShaderProgram;
import org.joml.Vector3f;

public abstract class Light {
    public static Vector3f DEFAULT_COLOUR = new Vector3f(1);

    public Vector3f ambient = DEFAULT_COLOUR.mul(.2f, new Vector3f());
    public Vector3f diffuse = DEFAULT_COLOUR;
    public Vector3f specular = DEFAULT_COLOUR;

    public String uniformName = "light";

    public void uniformValues(ShaderProgram sh) {
        sh.uniform3f(uniformName + ".ambient", ambient);
        sh.uniform3f(uniformName + ".diffuse", diffuse);
        sh.uniform3f(uniformName + ".specular", specular);
    };
}
