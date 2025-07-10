package boilerplate.rendering.light;

import boilerplate.rendering.ShaderProgram;
import org.joml.Vector3f;

public class PointLight extends Light {
    public Vector3f position;

    public float constant = 1;
    public float linear = .09f;
    public float quadratic = .032f;

    public PointLight(Vector3f position) {
        this.position = position;
    }

    @Override
    public void uniformValues(String uniform, ShaderProgram sh) {
        super.uniformValues(uniform, sh);
        sh.uniform3f(uniform + ".position", position);
        sh.uniform1f(uniform + ".constant", constant);
        sh.uniform1f(uniform + ".linear", linear);
        sh.uniform1f(uniform + ".quadratic", quadratic);
    }
}
