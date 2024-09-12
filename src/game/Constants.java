package src.game;

import java.awt.*;

public class Constants {
    public static final double EPSILON = 0.0001;
    public static final double EPSILON_SQ = EPSILON * EPSILON;
    public static final int FPS = 60;
    public static final double DT = 1 / (double) FPS;
    public static final String SHADERS_FOLDER = "src/shaders";

    public static final Dimension SCREEN_SIZE = new Dimension(900, 400);
}
