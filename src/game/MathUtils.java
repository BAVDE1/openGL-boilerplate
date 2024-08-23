package src.game;

public class MathUtils {
    public static double nanoToSecond(double nanoSecs) {
        return nanoSecs * 1E-9f;
    }

    public static double millisToSecond(double milliseconds) {
        return milliseconds * 1E-3f;
    }
}
