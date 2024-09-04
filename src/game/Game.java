package src.game;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL45;
import src.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    public Window window = new Window();  // handle
    public boolean optimiseTimeStepper = true;
    boolean vSync = false;  // probably keep off for now

    public double timeStarted = 0;
    public int secondsElapsed = 0;
    int frameCounter = 0;
    int fps = 0;

    public void start() {
        timeStarted = System.currentTimeMillis();
        Main.startTimeStepper(Constants.DT, this);
    }

    public void createCapabilitiesAndOpen() {
        window.setupContext();
        window.setVSync(vSync);
        bindEvents();

        GL.createCapabilities();  // critical for LWJGL's interoperation with GLFW's OpenGL context
        glClearColor(.0f, .0f, .0f, .0f);

        // for pixel perfect coordinates of vertices
        glOrtho(0, Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height, 0, -1, 1);

        window.show();
        setupShaders();
    }

    public void close() {
        System.out.println("Closing safely");
        glfwFreeCallbacks(window.handle);
        glfwDestroyWindow(window.handle);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(window.handle);
    }

    public void bindEvents() {
        // key inputs
        glfwSetKeyCallback(window.handle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);

            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_Q) {
                    this.window.setVSync(vSync = !vSync);
                }
            }
        });

        // window pos change
        glfwSetWindowPosCallback(window.handle, (window, xpos, ypos) -> System.out.printf("%s, %s%n", xpos, ypos));
    }

    /** Must be called after window is visible */
    public void setupShaders() {
        int program = GL45.glCreateProgram();

        File shaderFolder = new File(Constants.SHADERS_FOLDER);

        for (final File fileEntry : Objects.requireNonNull(shaderFolder.listFiles())) {
            int shaderType = -1;
            switch (fileEntry.getName()) {
                case "vertex" -> shaderType = GL45.GL_VERTEX_SHADER;
                case "geometry" -> shaderType = GL45.GL_GEOMETRY_SHADER;
                case "fragment" -> shaderType = GL45.GL_FRAGMENT_SHADER;
            }
            if (shaderType < 0 || fileEntry.isFile()) continue;

            File shaderTypeFolder = new File(Constants.SHADERS_FOLDER + fileEntry.getName());
            for (final  File shaderFile : )
            // get shader code
            String charSequence;
            try {
                Scanner scanner = new Scanner(fileEntry);
                StringBuilder fileContents = new StringBuilder();
                while (scanner.hasNextLine()) {
                    fileContents.append("/n").append(scanner.nextLine());
                }
                charSequence = fileContents.toString();
            } catch (FileNotFoundException e) {
                System.out.printf("'%s' at '%s' could not be read.\nError message: %s%n", fileEntry.getName(), fileEntry.getAbsolutePath(), e);
                continue;
            }

            // compile shader
            GL45.glShaderSource(shaderType, charSequence);
            GL45.glCompileShader(shaderType);

            int[] shaderCompiled = new int[1];  // only needs size of 1
            GL45.glGetShaderiv(shaderType, GL45.GL_COMPILE_STATUS, shaderCompiled);
            if (shaderCompiled[0] != GL_TRUE) {
                System.out.println(GL45.glGetShaderInfoLog(shaderType, 1024));
                continue;
            }

            // attach shader
            GL45.glAttachShader(program, shaderType);
            GL45.glLinkProgram(program);

            int[] programLinked = new int[1];
            GL45.glGetProgramiv(program, GL45.GL_LINK_STATUS, programLinked);
            if (programLinked[0] != GL_TRUE) {
                System.out.println(GL45.glGetProgramInfoLog(shaderType, 1024));
            }
        }
    }

    public void updateFps() {
        // update every second
        int newSeconds = (int) Math.floor(MathUtils.millisToSecond(System.currentTimeMillis()) - MathUtils.millisToSecond(timeStarted));
        if (newSeconds != secondsElapsed) {
            fps = frameCounter;
            frameCounter = 0;
            secondsElapsed = newSeconds;
            System.out.println(fps);
        }
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        // render here
        // switch to this https://docs.gl/gl2/glDrawArrays
        glBegin(GL_TRIANGLE_STRIP);  // https://docs.gl/gl2/glBegin
            glColor3f(1, 1, 0);
            glVertex2d(0, 0);
            glVertex2d(Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);
            glVertex2d(0, Constants.SCREEN_SIZE.height * .5);
        glEnd();

        glfwSwapBuffers(window.handle); // finish rendering
    }

    public double mainLoop(double dt) {
        double tStart = System.nanoTime();
        frameCounter++;

        updateFps();
        render();
        glfwPollEvents();

        return MathUtils.nanoToSecond(System.nanoTime() - tStart);
    }
}
