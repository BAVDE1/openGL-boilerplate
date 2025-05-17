package boilerplate.rendering;

import boilerplate.common.Window;
import boilerplate.utility.Logging;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Camera3d {
    public static class Action {
        public interface Func {
            void call(float speed);
        }

        public int key;
        Func callback;

        public Action(int key, Func actionFunc) {
            this.key = key;
            this.callback = actionFunc;
        }
    }

    public static final int MODE_FLY = 0;
    public static final int MODE_TARGET = 1;

    protected int mode;

    public Vector3f pos = new Vector3f(0, 0, 3);
    public Vector3f target = new Vector3f();

    public float pitch = 0;
    public float yaw = -90;  // (initial -90 to look along z axis)
    public float roll = 0;

    public Vector3f worldUp = new Vector3f(0, 1, 0);
    protected Vector3f front = new Vector3f();
    protected Vector3f right = new Vector3f();
    protected Vector3f up = new Vector3f();

    ArrayList<Action> keyMovementActions = new ArrayList<>(Arrays.asList(
            new Action(GLFW_KEY_W, speed -> pos.add(front.mul(speed, new Vector3f()))),
            new Action(GLFW_KEY_S, speed -> pos.sub(front.mul(speed, new Vector3f()))),
            new Action(GLFW_KEY_D, speed -> pos.add(right.mul(speed, new Vector3f()))),
            new Action(GLFW_KEY_A, speed -> pos.sub(right.mul(speed, new Vector3f()))),
            new Action(GLFW_KEY_E, speed -> pos.add(up.mul(speed, new Vector3f()))),
            new Action(GLFW_KEY_Q, speed -> pos.sub(up.mul(speed, new Vector3f()))),
            new Action(GLFW_KEY_SPACE, speed -> pos.add(0, speed, 0)),
            new Action(GLFW_KEY_LEFT_CONTROL, speed -> pos.sub(0, speed, 0))
    ));
    ArrayList<Action> keyRotationActions = new ArrayList<>(Arrays.asList(
            new Action(GLFW_KEY_UP, speed -> pitch += speed),
            new Action(GLFW_KEY_DOWN, speed -> pitch -= speed),
            new Action(GLFW_KEY_RIGHT, speed -> yaw += speed),
            new Action(GLFW_KEY_LEFT, speed -> yaw -= speed),
            new Action(GLFW_KEY_P, speed -> roll += speed),
            new Action(GLFW_KEY_O, speed -> roll -= speed)

            ));
    ArrayList<Action> mouseRotationActions = new ArrayList<>();

    public Camera3d(int mode) {
        this.mode = mode;
        calculateDirections();
    }

    public Camera3d(int mode, Vector3f initialPos) {
        this(mode);
        pos = new Vector3f(initialPos);
        calculateDirections();
    }

    public void processInput(Window window, double dt) {
        float speedMul = window.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 2 : 1;
        boolean rotUpdated = false;

        // rotation
        float rotSpeed = 70 * speedMul * (float) dt;
        for (Action action : keyRotationActions) {
            if (window.isKeyPressed(action.key)) {
                action.callback.call(rotSpeed);
                rotUpdated = true;
            }
        }

        if (rotUpdated) {
            pitch = Math.clamp(pitch, -89, 89);
            calculateDirections();
        }

        // movement
        float moveSpeed = 3 * speedMul * (float) dt;
        for (Action action : keyMovementActions) {
            if (window.isKeyPressed(action.key)) action.callback.call(moveSpeed);
        }
    }

    private Matrix4f generateTargetViewMatrix() {
        float radius = 5;
        float camX = (float) Math.sin(glfwGetTime()) * radius;
        float camZ = (float) Math.cos(glfwGetTime()) * radius;
        return new Matrix4f().lookAt(new Vector3f(camX, .0f, camZ), new Vector3f(), worldUp);
    }

    private Matrix4f generateFlyViewMatrix() {
        return new Matrix4f().lookAt(pos, pos.add(front, new Vector3f()), up);
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
        front.zero();
        float cPitch = (float) Math.cos(Math.toRadians(pitch));
        float sPitch = (float) Math.sin(Math.toRadians(pitch));
        float cYaw = (float) Math.cos(Math.toRadians(yaw));
        float sYaw = (float) Math.sin(Math.toRadians(yaw));
        float cRoll = (float) Math.cos(Math.toRadians(roll));
        float sRoll = (float) Math.sin(Math.toRadians(roll));
        front.x = cYaw * cPitch;
        front.y = sPitch;
        front.z = cPitch * sYaw;
        front.normalize();

//        up = new Vector3f(0, 1, 0);
        worldUp.rotateZ((float) Math.toRadians(roll), up);
//        front.rotateAxis((float) Math.toRadians(roll), up.x, up.y, 0);
//        up.rotateZ((float) Math.toRadians(roll));
//        front.rotateZ();
////        up.rotateX((float) Math.toRadians(roll));
////        up.rotateY((float) Math.toRadians(roll));
//        front.rotateAxis((float) Math.toRadians(roll), up.x, up.y, up.z);
        right = front.cross(up, new Vector3f()).normalize();
//        up = right.cross(front, new Vector3f()).normalize();
    }

    public void setMode(int newMode) {
        mode = newMode;
    }
}
