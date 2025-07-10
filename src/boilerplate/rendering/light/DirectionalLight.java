package boilerplate.rendering.light;

import boilerplate.rendering.ShaderProgram;
import org.joml.Vector3f;

public class DirectionalLight extends Light {
    public Vector3f direction;

    public DirectionalLight(Vector3f direction) {
        this.direction = direction;
    }

    @Override
    public void uniformValues(String uniform, ShaderProgram sh) {
        super.uniformValues(uniform, sh);
        sh.uniform3f(uniform + ".direction", direction);
    }
}
