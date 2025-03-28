package boilerplate.common;

public abstract class Game {
    public abstract void createCapabilitiesAndOpen();
    /** returns the time main loop took in seconds */
    public abstract double mainLoop(double staticDt);
    public abstract boolean shouldClose();
    public abstract void close();
}
