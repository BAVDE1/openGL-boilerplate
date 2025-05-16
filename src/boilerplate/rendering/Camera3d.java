package boilerplate.rendering;

import boilerplate.common.Window;
import boilerplate.utility.Logging;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Camera3d {
    public static final int MODE_FLY = 0;
    public static final int MODE_TARGET = 1;

    protected int mode;

    public Vector3f pos = new Vector3f(0, 0, 3);
    public Vector3f target = new Vector3f();
    public Vector3f direction = new Vector3f();

    public float pitch = 0;  // along y (vertical)
    public float yaw = -90;  // along x (horizontal)  (initial -90 to look along z axis)
    public float roll = 0;  // along z

    public Vector3f worldUp = new Vector3f(0, 1, 0);
    protected Vector3f front = new Vector3f(0, 0, -1);
    protected Vector3f right = new Vector3f();
    protected Vector3f up = new Vector3f();

    public Camera3d(int mode) {
        this.mode = mode;
    }

    public Camera3d(int mode, Vector3f initialPos) {
        this(mode);
        pos = new Vector3f(initialPos);
    }

    public void processInput(Window window, double dt) {
        // rotation


        // movement
        float moveSpeed = window.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 5 : 3;
        moveSpeed *= (float) dt;
        if (window.isKeyPressed(GLFW_KEY_W)) {
            pos.add(front.mul(moveSpeed, new Vector3f()));
        }
        if (window.isKeyPressed(GLFW_KEY_S)) {
            pos.sub(front.mul(moveSpeed, new Vector3f()));
        }
        if (window.isKeyPressed(GLFW_KEY_D)) {
            pos.add(front.cross(worldUp, new Vector3f()).mul(moveSpeed));
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            pos.sub(front.cross(worldUp, new Vector3f()).mul(moveSpeed));
        }
        if (window.isKeyPressed(GLFW_KEY_E) || window.isKeyPressed(GLFW_KEY_SPACE)) {
            pos.add(0, moveSpeed, 0);
        }
        if (window.isKeyPressed(GLFW_KEY_Q) || window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
            pos.sub(0, moveSpeed, 0);
        }
    }

    private Matrix4f generateTargetViewMatrix() {
        calculateDirections();
        float radius = 5;
        float camX = (float) Math.sin(glfwGetTime()) * radius;
        float camZ = (float) Math.cos(glfwGetTime()) * radius;
        Matrix4f view = new Matrix4f().lookAt(new Vector3f(camX, .0f, camZ), new Vector3f(), worldUp);
        return view;
    }

    private Matrix4f generateFlyViewMatrix() {
        return new Matrix4f().lookAt(pos, pos.add(front, new Vector3f()), worldUp);
    }

    public Matrix4f generateViewMatrix() {
        return switch (mode) {
            case MODE_FLY -> generateFlyViewMatrix();
            case MODE_TARGET -> generateTargetViewMatrix();
            default -> {
                Logging.danger("This camera's mode is invalid! current mode: (%s)", mode);
                yield new Matrix4f().identity();
            }
        };
    }

    private void calculateDirections() {
        direction = pos.sub(target, new Vector3f()).normalize();
        right = worldUp.cross(direction, new Vector3f()).normalize();
        up = direction.cross(right, new Vector3f());
    }

    public void setMode(int newMode) {
        mode = newMode;
    }
}
