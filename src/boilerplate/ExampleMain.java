package boilerplate;

import boilerplate.common.BoilerplateConstants;

import boilerplate.example.Example2d;
import boilerplate.example.Example3d;
import boilerplate.example.ExampleIndex;
import boilerplate.utility.Logging;

public class ExampleMain {
    public static void main(String[] args) {
        Logging.setupFileLogging(BoilerplateConstants.LOGGING_FILE_NAME);
        Logging.mystical("Starting program (opengl-boilerplate)");
        new ExampleIndex().start();
    }
}
