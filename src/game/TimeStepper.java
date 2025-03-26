package src.game;

import src.utility.Logging;
import src.utility.MathUtils;

public class TimeStepper {
    /**
     * Proper time stepper.
     * if game has optimised boolean toggled, thread sleeps for half of dt once stepped.
     */
    public static void startTimeStepper(double static_dt, Game game) {
        final double halfDt = static_dt * 0.5;  // in seconds
        double accumulator = 0;
        double lastFrame = System.nanoTime();

        game.createCapabilitiesAndOpen();
        Logging.debug("Starting time stepper with a dt of %s", static_dt);

        while (!game.shouldClose()) {
            double t = System.nanoTime();
            accumulator += MathUtils.nanoToSecond(t - lastFrame);
            accumulator = Math.min(1, accumulator);  // min of 1 fps
            lastFrame = t;

            while (accumulator >= static_dt) {
                accumulator -= static_dt;

                try {
                    double loopTime = game.mainLoop(static_dt);  // in seconds
                    if (Constants.OPTIMIZE_TIME_STEPPER && accumulator + loopTime < halfDt) {  // only sleep if there is enough time
                        Thread.sleep((long) Math.floor(halfDt * 1_000));  // give it a little break *-*
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Program closed while thread was asleep (between frames)");
                }
            }
        }
        game.close();
    }
}
