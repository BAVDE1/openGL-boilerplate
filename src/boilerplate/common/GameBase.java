package boilerplate.common;

public abstract class GameBase {
    public abstract void start() throws Exception;
    public abstract void createCapabilitiesAndOpen();
    public abstract void mainLoop(double staticDt);
    public abstract boolean shouldClose();
    public abstract void close();
}
