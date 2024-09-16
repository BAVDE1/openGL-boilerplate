package src.utility;

import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GLDebugMessageCallbackI;
import src.game.Constants;

import java.io.PrintStream;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.lwjgl.system.MemoryUtil.memByteBuffer;

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

    public static GLDebugMessageCallbackI debugCallback() {
        return (source, type, id, severity, length, message, userParam) -> {
            String format = "Source: %s\n    Type: %s\n    Severity: %s\n    %s";

            String sourceStr = getCallbackSourceString(source);
            String typeStr = getCallbackTypeString(type);
            String severityStr = getCallbackSeverityString(severity);

            CharBuffer decodedMeg = StandardCharsets.UTF_8.decode(memByteBuffer(message, length));
            String finalMsg = String.format(format, sourceStr, typeStr, severityStr, decodedMeg);

            switch (severity) {
                case GL45.GL_DEBUG_SEVERITY_LOW, GL45.GL_DEBUG_SEVERITY_MEDIUM -> warn(finalMsg);
                case GL45.GL_DEBUG_SEVERITY_HIGH -> danger(finalMsg);
                case GL45.GL_DEBUG_SEVERITY_NOTIFICATION -> info(finalMsg);
            }
        };
    }

    private static String getCallbackTypeString(int type) {
        String typeStr = "";
        switch (type) {
            case GL45.GL_DEBUG_TYPE_ERROR -> typeStr = "Error";
            case GL45.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> typeStr = "Deprecated Behaviour";
            case GL45.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> typeStr = "Undefined Behaviour";
            case GL45.GL_DEBUG_TYPE_PORTABILITY -> typeStr = "Portability";
            case GL45.GL_DEBUG_TYPE_PERFORMANCE -> typeStr = "Performance";
            case GL45.GL_DEBUG_TYPE_OTHER -> typeStr = "Other";
            case GL45.GL_DEBUG_TYPE_MARKER -> typeStr = "Marker";
        }
        return typeStr;
    }

    private static String getCallbackSourceString(int source) {
        String sourceStr = "";
        switch (source) {
            case GL45.GL_DEBUG_SOURCE_APPLICATION -> sourceStr = "Application";
            case GL45.GL_DEBUG_SOURCE_API -> sourceStr = "API";
            case GL45.GL_DEBUG_SOURCE_SHADER_COMPILER -> sourceStr = "Shader Compiler";
            case GL45.GL_DEBUG_SOURCE_THIRD_PARTY -> sourceStr = "Third Party";
            case GL45.GL_DEBUG_SOURCE_WINDOW_SYSTEM -> sourceStr = "Window System";
            case GL45.GL_DEBUG_SOURCE_OTHER -> sourceStr = "Other";
        }
        return sourceStr;
    }

    private static String getCallbackSeverityString(int severity) {
        String severityStr = "";
        switch (severity) {
            case GL45.GL_DEBUG_SEVERITY_LOW -> severityStr = "LOW";
            case GL45.GL_DEBUG_SEVERITY_MEDIUM -> severityStr = "MEDIUM";
            case GL45.GL_DEBUG_SEVERITY_HIGH -> severityStr = "HIGH";
            case GL45.GL_DEBUG_SEVERITY_NOTIFICATION -> severityStr = "NOTIFICATION";
        }
        return severityStr;
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
