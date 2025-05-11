package boilerplate.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera3d {
    public Vector3f pos = new Vector3f();
    public Vector3f target = new Vector3f();

    public float pitch = 0;
    public float yaw = 0;
    public float roll = 0;

    public Vector3f worldUp = new Vector3f(0, 1, 0);
    private Vector3f front = new Vector3f();
    private Vector3f right = new Vector3f();
    private Vector3f up = new Vector3f();

    public Camera3d() {}

    public Camera3d(Vector3f initialPos) {
        pos = new Vector3f(initialPos);
    }

    public Matrix4f generateViewMatrix() {
        calculateDirections();
        Matrix4f view = new Matrix4f().identity();
        return view.translate(pos);
    }

    private void calculateDirections() {
        // todo
    }
}
