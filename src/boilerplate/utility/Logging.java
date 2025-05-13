package boilerplate.utility;

import boilerplate.common.BoilerplateConstants;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GLDebugMessageCallbackI;

import java.io.*;
import java.lang.reflect.AnnotatedArrayType;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.memByteBuffer;

/**
 * Static logger
 */
public class Logging {
    private static final ArrayList<Integer> ignoreList = new ArrayList<>(List.of(2));
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final int tracebackInx = 3;

    // more ansi escape codes: https://talyian.github.io/ansicolors/
    private static final String black = "30";
    private static final String red = "31";
    private static final String green = "32";
    private static final String yellow = "38;5;11";
    private static final String blue = "38;5;51";
    private static final String purple = "38;5;98";
    private static final String pink = "38;5;201";
    private static final String grey = "37";
    private static final String white = "2";

    private static boolean isFileLogging = false;
    public static String defaultLoggingDir = "logs";
    public static Boolean silenceLogs = false;
    public static Boolean logDebug = true;

    private static void log(String col, String msg, String level, Object... args) {
        if (silenceLogs) return;
        String colorFormat = isFileLogging ? "%s %s [%s:%s] %s%n" : "\u001B[" + col + "m%s %s [%s:%s] %s\u001B[0m%n";
        String callerFile = Thread.currentThread().getStackTrace()[tracebackInx].getFileName();
        int callersLineNumber = Thread.currentThread().getStackTrace()[tracebackInx].getLineNumber();
        System.out.printf(
                colorFormat,
                LocalTime.now().format(dateTimeFormat),
                level,
                callerFile,
                callersLineNumber,
                String.format(msg, args)
        );
    }

    public static void debug(Object... args) {
        debug(buildStringForArgs(args), args);
    }

    public static void debug(String msg, Object... args) {
        if (logDebug) log(grey, msg, "DEBUG", args);
    }

    public static void info(Object... args) {
        info(buildStringForArgs(args), args);
    }

    public static void info(String msg, Object... args) {
        log(white, msg, "INFO", args);
    }

    public static void warn(Object... args) {
        warn(buildStringForArgs(args), args);
    }

    public static void warn(String msg, Object... args) {
        log(yellow, msg, "WARN", args);
    }

    public static void danger(Object... args) {
        danger(buildStringForArgs(args), args);
    }

    public static void danger(String msg, Object... args) {
        log(red, msg, "DANGER", args);
    }

    public static void mystical(Object... args) {
        mystical(buildStringForArgs(args), args);
    }

    public static void mystical(String msg, Object... args) {
        log(blue, msg, "MYSTICAL", args);
    }

    public static void purple(Object... args) {
        purple(buildStringForArgs(args), args);
    }

    public static void purple(String msg, Object... args) {
        log(purple, msg, "EXPENSIVE", args);
    }

    private static String buildStringForArgs(Object... args) {
        StringBuilder s = new StringBuilder();
        for (Object arg : args) {
            if (!s.isEmpty()) s.append(", ");
            s.append("%s");
        }
        return s.toString();
    }

    public static void setupFileLogging(String fileName) {
        try {
            String jarFolder = BoilerplateConstants.getJarFolder();
            if (jarFolder == null) {
                debug("File logging aborted, not a valid jar file.");
                return;
            }

            String loggingDir = String.format("%s/%s", jarFolder, defaultLoggingDir);
            File loggingDirFile = new File(loggingDir);
            if (!loggingDirFile.exists()) {
                if (!loggingDirFile.mkdirs()) danger("Error making directory: %s", loggingDir);
            }

            PrintStream ps = new PrintStream(String.format("%s/%s", loggingDir, fileName));
            System.setOut(ps);
            System.setErr(ps);
            isFileLogging = true;
        } catch (IOException e) {
            danger("Error setting up file logging:\n%s", e);
        }
    }

    public static GLDebugMessageCallbackI debugCallback() {
        return (source, type, id, severity, length, message, userParam) -> {
            String sourceStr = getCallbackSourceString(source);
            String typeStr = getCallbackTypeString(type);
            String severityStr = getCallbackSeverityString(severity);

            String decodedMsg = String.valueOf(StandardCharsets.UTF_8.decode(memByteBuffer(message, length)));
            if (ignoreList.contains(id)) {
                debug("Suppressing warning id: %s (src: %s, type: %s, severity: %s, msg: %s)",
                        id, sourceStr, typeStr, severityStr, decodedMsg.replaceAll("\n", ". "));
                return;
            }

            String finalMsg = String.format("Source: %s (id: %s)\n    Type: (%s) %s\n    Severity: %s\n    %s",
                    sourceStr, id, type, typeStr, severityStr, decodedMsg);

            switch (severity) {
                case GL45.GL_DEBUG_SEVERITY_LOW, GL45.GL_DEBUG_SEVERITY_MEDIUM -> warn(finalMsg);
                case GL45.GL_DEBUG_SEVERITY_HIGH -> danger(finalMsg);
                case GL45.GL_DEBUG_SEVERITY_NOTIFICATION -> debug(finalMsg);
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
