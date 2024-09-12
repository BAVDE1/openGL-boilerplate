package src.game;

import org.lwjgl.opengl.GL45;
import src.utility.Logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.*;

public class ShaderHelper {
    /** Search entire directory (including folders within folders) until a file is found, and pass it to applyShader() */
    public static void attachShadersInDir(File dir, int program) {
        for (final File fileEntry : Objects.requireNonNull(dir.listFiles())) {
            if (fileEntry.isDirectory()) {
                if (fileEntry.getName().equals("ignore")) continue;

                attachShadersInDir(fileEntry.getAbsoluteFile(), program);
                continue;
            }
            attachShader(fileEntry, program);
        }
    }

    /**
     * adds new GL shader from given file (if applicable)
     * <a href="https://docs.gl/gl2/glAttachShader">Shader setup example</a>
     */
    public static void attachShader(File file, int program) {
        int shaderType = getShaderType(file);
        if (shaderType < 0) return;  // not a shader file

        // get file contents
        String charSequence;
        try {
            Scanner scanner = new Scanner(file);
            StringBuilder fileContents = new StringBuilder();
            while (scanner.hasNextLine()) {
                fileContents.append("\n").append(scanner.nextLine());
            }
            charSequence = fileContents.toString();
        } catch (FileNotFoundException e) {
            Logging.danger(String.format("'%s' at '%s' could not be read.\nError message: %s%n", file.getName(), file.getAbsolutePath(), e));
            return;
        }

        // compile shader
        int shader = GL45.glCreateShader(shaderType);
        GL45.glShaderSource(shader, charSequence);

        GL45.glCompileShader(shader);
        int[] shaderCompiled = new int[1];  // only needs size of 1
        GL45.glGetShaderiv(shader, GL45.GL_COMPILE_STATUS, shaderCompiled);
        if (shaderCompiled[0] != GL_TRUE) {
            Logging.danger(String.format("Shader Compile Error (%s): %s", file, GL45.glGetShaderInfoLog(shader, 1024)));
            return;
        }

        GL45.glAttachShader(program, shader);
        Logging.info(String.format("Shader Attached: '%s', %s chars (type %s)", file, charSequence.length(), shaderType));
    }

    public static void linkProgram(int program) {
        GL45.glLinkProgram(program);
        int[] programLinked = new int[1];

        GL45.glGetProgramiv(program, GL45.GL_LINK_STATUS, programLinked);
        if (programLinked[0] != GL_TRUE) {
            Logging.danger(String.format("Shader Linking Error: %s", GL45.glGetProgramInfoLog(program, 1024)));
        }
    }

    /** <a href="https://www.khronos.org/opengl/wiki/Shader">OpenGL shaders</a> */
    private static int getShaderType(File file) {
        String[] splitName = file.getName().split("\\.");
        String ext = splitName[splitName.length - 1];

        int shaderType = -1;
        switch (ext) {
            case "vert" -> shaderType = GL45.GL_VERTEX_SHADER;
            case "tesc" -> shaderType = GL45.GL_TESS_CONTROL_SHADER;
            case "tese" -> shaderType = GL45.GL_TESS_EVALUATION_SHADER;
            case "geom" -> shaderType = GL45.GL_GEOMETRY_SHADER;
            case "frag" -> shaderType = GL45.GL_FRAGMENT_SHADER;
        }
        return shaderType;
    }
}
