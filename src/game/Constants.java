package src.game;

import java.awt.*;

public class Constants {
    public static final Boolean logDebug = true;

    public static final int BUFF_SIZE_SMALL    = 1024;
    public static final int BUFF_SIZE_MEDIUM   = BUFF_SIZE_SMALL   * 2;
    public static final int BUFF_SIZE_LARGE    = BUFF_SIZE_MEDIUM  * 2;
    public static final int BUFF_SIZE_LARGER   = BUFF_SIZE_LARGE   * 2;
    public static final int BUFF_SIZE_LARGEST  = BUFF_SIZE_LARGER  * 2;
    public static final int BUFF_SIZE_ENORMOUS = BUFF_SIZE_LARGEST * 2;

    public static final int MODE_NIL = 0;
    public static final int MODE_TEX = 1;
    public static final int MODE_COL = 2;

    public static final double EPSILON = 0.0001;
    public static final double EPSILON_SQ = EPSILON * EPSILON;
    public static final int FPS = 60;
    public static final double DT = 1 / (double) FPS;

    public static final String SHADERS_FOLDER = "res/shaders";

    public static final Dimension SCREEN_SIZE = new Dimension(900, 400);
    public static final boolean OPTIMIZE_TIME_STEPPER = true;
    public static final boolean V_SYNC = false;

    public static int findNextLargestBuffSize(int givenSize) {
        int[] allSizes = new int[] {BUFF_SIZE_SMALL, BUFF_SIZE_MEDIUM, BUFF_SIZE_LARGE, BUFF_SIZE_LARGER, BUFF_SIZE_LARGEST};
        for (int size : allSizes) {
            if (size > givenSize) return size;
        }
        return BUFF_SIZE_ENORMOUS;
    }
}
