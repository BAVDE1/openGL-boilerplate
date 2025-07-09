package boilerplate.rendering.light;

import boilerplate.rendering.ShaderProgram;
import boilerplate.utility.MathUtils;
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
    public void uniformValues(ShaderProgram sh) {
        super.uniformValues(sh);
        sh.uniform3f(uniformStructName + ".position", position);
        sh.uniform3f(uniformStructName + ".direction", direction);
        sh.uniform1f(uniformStructName + ".cutoff", (float) Math.cos(Math.toRadians(cutoff)));
    }
}
