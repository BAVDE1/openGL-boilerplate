package boilerplate.rendering.light;

import boilerplate.rendering.ShaderProgram;
import org.joml.Vector3f;

public class SpotLight extends PointLight {
    public Vector3f direction;
    float cutoff = 10;
    float outerCutoff = 12;

    public SpotLight(Vector3f position, Vector3f direction) {
        super(position);
        this.direction = direction;
    }

    public SpotLight(Vector3f position, Vector3f direction, float cutoff) {
        this(position, direction);
        this.cutoff = cutoff;
    }

    public SpotLight(Vector3f position, Vector3f direction, float cutoff, float outerCutoff) {
        this(position, direction, cutoff);
        this.outerCutoff = outerCutoff;
    }

    @Override
    public void uniformValues(String uniform, ShaderProgram sh) {
        super.uniformValues(uniform, sh);
        sh.uniform3f(uniform + ".direction", direction);
        sh.uniform1f(uniform + ".cutoff", (float) Math.cos(Math.toRadians(cutoff)));
        sh.uniform1f(uniform + ".outerCutoff", (float) Math.cos(Math.toRadians(outerCutoff)));
    }
}
