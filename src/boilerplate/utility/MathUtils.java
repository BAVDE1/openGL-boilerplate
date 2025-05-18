package boilerplate.utility;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class MathUtils {
    public static double INV_PI = 1 / Math.PI;
    public static double INV_180 = 1 / 180d;
    public static int MATRIX4F_BYTES_SIZE = Float.BYTES * 4 * 4;

    public static double nanoToSecond(double nanoSecs) {
        return nanoSecs * 1E-9f;
    }
    public static double millisToSecond(double milliseconds) {
        return milliseconds * 1E-3f;
    }

    public static float roundToPlace(float num, int place) {
        return (float) Math.round(num * place) / place;
    }

    public static FloatBuffer matrixToBuff(Matrix4f m) {
        FloatBuffer data = BufferUtils.createFloatBuffer(16);
        m.get(data);
        return data;
    }
}
