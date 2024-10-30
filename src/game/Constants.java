package src.game;

import src.utility.Logging;

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

    public static final int ERROR = -1;
    public static final double EPSILON = 0.0001;
    public static final float THREE_SQRT = 1.7321f;
    public static final double EPSILON_SQ = EPSILON * EPSILON;
    public static final int FPS = 60;
    public static final double DT = 1 / (double) FPS;

    public static final String SHADER_VERTEX = "res/shaders/vs.vert";
    public static final String SHADER_FRAGMENT = "res/shaders/fs.frag";
    public static final String SHADERS_FOLDER = "res/shaders";

    public static final Dimension SCREEN_SIZE = new Dimension(900, 400);
    public static final boolean OPTIMIZE_TIME_STEPPER = true;
    public static final boolean V_SYNC = false;

    public static int findNextLargestBuffSize(int givenSize) {
        if (givenSize >= BUFF_SIZE_ENORMOUS) {
            Logging.danger("Maximum buffer size reached! Attempting to find a size greater than the given '%s', which doesn't currently exist.", givenSize);
            return ERROR;
        }

        int[] allSizes = new int[] {BUFF_SIZE_SMALL, BUFF_SIZE_MEDIUM, BUFF_SIZE_LARGE, BUFF_SIZE_LARGER, BUFF_SIZE_LARGEST};
        for (int size : allSizes) {
            if (size > givenSize) return size;
        }
        return BUFF_SIZE_ENORMOUS;
    }
}
