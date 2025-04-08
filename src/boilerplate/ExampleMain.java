package boilerplate;

import boilerplate.common.BoilerplateConstants;

import boilerplate.example.ExampleGame;
import boilerplate.utility.Logging;

public class ExampleMain {
    public static void main(String[] args) {
        Logging.setupFileLogging(BoilerplateConstants.LOGGING_FILE_NAME);
        Logging.mystical("Starting program (lwjgl-boilerplate-version: %s)", BoilerplateConstants.VERSION);
        new ExampleGame().start();
    }
}
