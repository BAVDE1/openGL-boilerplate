package src.utility;

import src.game.Constants;

import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logging {
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final int tracebackInx = 3;

    // more ansi escape codes: https://talyian.github.io/ansicolors/
    private static final String black = "30";
    private static final String red = "31";
    private static final String green = "32";
    private static final String yellow = "38;5;11";
    private static final String blue = "34";
    private static final String magenta = "35";
    private static final String cyan = "36";
    private static final String grey = "37";
    private static final String white = "2";

    private static void log(String col, String msg, String level) {
        String callerFile = Thread.currentThread().getStackTrace()[tracebackInx].getFileName();
        int callersLineNumber = Thread.currentThread().getStackTrace()[tracebackInx].getLineNumber();
        System.out.printf("\u001B[" + col + "m%s %s [%s:%s] %s\u001B[0m%n", LocalTime.now().format(format), level, callerFile, callersLineNumber, msg);
    }

    public static void info(String msg) {
        log(white, msg, "INFO");
    }

    public static void debug(String msg) {
        if (Constants.logDebug) {
            log(grey, msg, "DEBUG");
        }
    }

    public static void warn(String msg) {
        log(yellow, msg, "WARN");
    }

    public static void danger(String msg) {
        log(red, msg, "DANGER");
    }

    public static PrintStream errStream() {
        return new PrintStream(System.err) {
            @Override
            public void print(Object obj) {
                danger(String.valueOf(obj));
            }
        };
    }
}
