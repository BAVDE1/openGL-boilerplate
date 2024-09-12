package src.utility;

import src.game.Constants;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {
    private static final Logger logger = Logger.getLogger("logger");

    // ansi escape codes
    private static final String black = "30";
    private static final String red = "31";
    private static final String green = "32";
    private static final String yellow = "38;5;11";
    private static final String blue = "34";
    private static final String magenta = "35";
    private static final String cyan = "36";
    private static final String grey = "37";
    private static final String white = "2";

    private static void log(String msg) {
        String callerFile = Thread.currentThread().getStackTrace()[3].getFileName();
        int callersLineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
        logger.log(Level.INFO, String.format("<%s:%s> %s", callerFile, callersLineNumber, msg));
    }

    private static void setFormat(String col) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "\u001B[" + col + "m[%1$tT] %4$s %5$s%6$s%n");
    }

    public static void info(String msg) {
        setFormat(white);
        log(msg);
    }

    public static void debug(String msg) {
        if (Constants.logDebug) {
            setFormat(grey);
            log(msg);
        }
    }

    public static void warn(String msg) {
        setFormat(yellow);
        log(msg);
    }

    public static void danger(String msg) {
        setFormat(red);
        log(msg);
    }
}
