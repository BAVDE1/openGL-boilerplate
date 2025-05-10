package boilerplate.utility;

public class MathUtils {
    public static double INV_PI = 1 / Math.PI;
    public static double INV_180 = 1 / 180d;

    public static double nanoToSecond(double nanoSecs) {
        return nanoSecs * 1E-9f;
    }
    public static double millisToSecond(double milliseconds) {
        return milliseconds * 1E-3f;
    }

    public static float roundToPlace(float num, int place) {
        return (float) Math.round(num * place) / place;
    }

    public static double degToRad(int deg) {
        return deg * (Math.PI * INV_180);
    }

    public static double radToDeg(int rad) {
        return rad * (180 * INV_PI);
    }
}
