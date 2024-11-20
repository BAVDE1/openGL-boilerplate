package src.rendering;

import src.game.Constants;
import src.utility.Logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.lwjgl.opengl.GL45.*;

/**
 * Loads, compiles and links shaders from file / file paths to its own shader program.
 */
public class ShaderHelper {
    public final HashMap<String, Integer> uniformCache = new HashMap<>();
    public final HashMap<Integer, String> attachedShaders = new HashMap<>();

    private Integer program;
    private boolean linked = false;

    public void genProgram() {
        if (program != null) {
            Logging.warn("Attempting to re-generate already generated program, aborting");
            return;
        }
        program = glCreateProgram();
    }

    /** Search entire directory (including folders within folders) until a file is found, and pass it to applyShader() */
    public void attachShadersInDir(File dir) {
        for (final File fileEntry : Objects.requireNonNull(dir.listFiles())) {
            if (fileEntry.isDirectory()) {
                if (fileEntry.getName().equals("ignore")) continue;
                attachShadersInDir(fileEntry.getAbsoluteFile());
                continue;
            }
            attachShader(fileEntry);
        }
    }

    /** Attach the 2 necessary shaders */
    public void attachShaders(String vertexFilePath, String fragmentFilePath) {
        File vertexFile = new File(vertexFilePath);
        File fragmentFile = new File(fragmentFilePath);
        attachShader(vertexFile);
        attachShader(fragmentFile);
    }

    /**
     * adds new GL shader from given file (if applicable)
     * <a href="https://docs.gl/gl2/glAttachShader">Shader setup example</a>
     */
    public void attachShader(File file) {
        if (program == null || program < 0) {
            Logging.danger("No program set or program failed to be created.");
            return;
        }

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
            Logging.danger("'%s' at '%s' could not be read.\nError message: %s%n", file.getName(), file.getAbsolutePath(), e);
            return;
        }

        // compile shader
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, charSequence);

        glCompileShader(shader);
        int[] shaderCompiled = new int[1];  // only needs size of 1
        glGetShaderiv(shader, GL_COMPILE_STATUS, shaderCompiled);
        if (shaderCompiled[0] != GL_TRUE) {
            Logging.danger("Shader Compile Error (%s): %s", file, glGetShaderInfoLog(shader, 1024));
            return;
        }

        glAttachShader(program, shader);
        attachedShaders.put(shader, file.getName());
    }

    public void linkProgram() {
        if (linked) return;

        linked = true;
        glLinkProgram(program);
        int[] programLinked = new int[1];

        glGetProgramiv(program, GL_LINK_STATUS, programLinked);
        if (programLinked[0] != GL_TRUE) {
            Logging.danger("Shader Linking Error: %s", glGetProgramInfoLog(program, 1024));
            return;
        }
        Logging.debug("Shaders %s [linked with program %s]", attachedShaders, program);
    }

    /** <a href="https://www.khronos.org/opengl/wiki/Shader">OpenGL shaders</a> */
    private static int getShaderType(File file) {
        String[] splitName = file.getName().split("\\.");
        String ext = splitName[splitName.length - 1];

        int shaderType = -1;
        switch (ext) {
            case "vert" -> shaderType = GL_VERTEX_SHADER;
            case "tesc" -> shaderType = GL_TESS_CONTROL_SHADER;
            case "tese" -> shaderType = GL_TESS_EVALUATION_SHADER;
            case "geom" -> shaderType = GL_GEOMETRY_SHADER;
            case "frag" -> shaderType = GL_FRAGMENT_SHADER;
        }
        return shaderType;
    }

    public static void uniformResolutionData(ShaderHelper sh) {
        ShaderHelper.uniform2f(sh, "resolution", Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);
        ShaderHelper.uniformMatrix4f(sh, "projectionMatrix", Constants.PROJECTION_MATRIX);
    }

    public void bind() {Renderer.bindShader(this);}
    public void unbind() {Renderer.unBindShader();}
    public int getProgram() {return program;}

    public int getUniformLocation(String uniform) {
        if (!linked) {
            Logging.danger("Shader has not been linked!");
            return -1;
        }

        bind();
        if (!uniformCache.containsKey(uniform)) {
            uniformCache.put(uniform, glGetUniformLocation(program, uniform));
        }
        return uniformCache.get(uniform);
    }

    public static void uniform1i(ShaderHelper sh, String uniform, int i) {
        glUniform1i(sh.getUniformLocation(uniform), i);
    }
    public static void uniform1iv(ShaderHelper sh, String uniform, int[] intArray) {
        glUniform1iv(sh.getUniformLocation(uniform), intArray);
    }
    public static void uniform1f(ShaderHelper sh, String uniform, float f) {
        glUniform1f(sh.getUniformLocation(uniform), f);
    }
    public static void uniform2f(ShaderHelper sh, String uniform, float f1, float f2) {
        glUniform2f(sh.getUniformLocation(uniform), f1, f2);
    }
    public static void uniformMatrix4f(ShaderHelper sh, String uniform, float[] matrix4f) {
        if (matrix4f.length != 4*4) {
            Logging.danger("matrix4 given does not have exactly %s items", 4*4);
            return;
        }
        glUniformMatrix4fv(sh.getUniformLocation(uniform), false, matrix4f);
    }
}
