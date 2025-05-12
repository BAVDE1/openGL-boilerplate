package boilerplate.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Camera3d {
    public Vector3f pos = new Vector3f();
    public Vector3f target = new Vector3f();
    public Vector3f direction = new Vector3f();

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

    @Deprecated
    public Matrix4f generateTargetedViewMatrix() {
        calculateDirections();
        float radius = 5;
        float camX = (float) Math.sin(glfwGetTime()) * radius;
        float camZ = (float) Math.cos(glfwGetTime()) * radius;
        Matrix4f view = new Matrix4f().lookAt(new Vector3f(camX, .0f, camZ), new Vector3f(), worldUp);
        return view;
    }

    public Matrix4f generateViewMatrix() {
        Matrix4f view = new Matrix4f();
        return view;
    }

    private void calculateDirections() {
        direction = pos.sub(target, new Vector3f()).normalize();
        right = worldUp.cross(direction, new Vector3f()).normalize();
        up = direction.cross(right, new Vector3f());
    }
}
