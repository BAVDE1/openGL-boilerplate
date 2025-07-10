package boilerplate.rendering.light;

import boilerplate.rendering.ShaderProgram;
import org.joml.Vector3f;

public class SpotLight extends Light {
    public Vector3f position;
    public Vector3f direction;
    float cutoff = 15;

    public SpotLight(Vector3f position, Vector3f direction) {
        this.position = position;
        this.direction = direction;
    }

    @Override
    public void uniformValues(String uniform, ShaderProgram sh) {
        super.uniformValues(uniform, sh);
        sh.uniform3f(uniform + ".position", position);
        sh.uniform3f(uniform + ".direction", direction);
        sh.uniform1f(uniform + ".cutoff", (float) Math.cos(Math.toRadians(cutoff)));
    }
}
