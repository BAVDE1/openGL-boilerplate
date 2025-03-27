import common.Constants;
import common.ExampleGame;

import utility.Logging;

public class ExampleMain {
    public static void main(String[] args) {
        Logging.mystical("Starting program (lwjgl-boilerplate-version: %s)", Constants.VERSION);
        new ExampleGame().start();
    }
}
