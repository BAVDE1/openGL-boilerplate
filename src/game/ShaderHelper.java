package src.game;

import org.lwjgl.opengl.GL45;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.*;

public class ShaderHelper {
    /** Search entire directory (including folders within folders) until a file is found, and pass it to applyShader() */
    public static void searchDirectory(File dir, int program) {
        for (final File fileEntry : Objects.requireNonNull(dir.listFiles())) {
            if (fileEntry.isDirectory()) {
                searchDirectory(fileEntry.getAbsoluteFile(), program);
                continue;
            }
            applyShader(fileEntry, program);
        }
    }

    /** adds new GL shader from given file (if applicable) */
    public static void applyShader(File file, int program) {
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
            System.out.printf("'%s' at '%s' could not be read.\nError message: %s%n", file.getName(), file.getAbsolutePath(), e);
            return;
        }

        // compile shader
        int shader = GL45.glCreateShader(shaderType);
        GL45.glShaderSource(shader, charSequence);
        GL45.glCompileShader(shader);

        int[] shaderCompiled = new int[1];  // only needs size of 1
        GL45.glGetShaderiv(shader, GL45.GL_COMPILE_STATUS, shaderCompiled);
        if (shaderCompiled[0] != GL_TRUE) {
            System.out.printf("Shader Compile Error: %s%n", GL45.glGetShaderInfoLog(shader, 1024));
            return;
        }

        // attach shader
        GL45.glAttachShader(program, shader);
        GL45.glLinkProgram(program);

        int[] programLinked = new int[1];
        GL45.glGetProgramiv(program, GL45.GL_LINK_STATUS, programLinked);
        if (programLinked[0] != GL_TRUE) {
            System.out.printf("Shader Linking Error: %s%n", GL45.glGetProgramInfoLog(shader, 1024));
            return;
        }
        System.out.printf("Shader Attached: '%s', %s chars (type %s)%n", file, charSequence.length(), shaderType);
    }

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
