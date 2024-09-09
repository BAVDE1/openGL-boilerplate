package src.game;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;
import src.Main;

import java.io.File;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    public Window window = new Window();  // handle
    public int program;
    public boolean optimiseTimeStepper = true;
    boolean vSync = false;  // probably keep off for now

    public double timeStarted = 0;
    public int secondsElapsed = 0;
    int frameCounter = 0;
    int fps = 0;

    int uniformTimeInx;

    int vertBuff1;
    int VAO;

    float[] verts = {
            50, 50,
            Constants.SCREEN_SIZE.width, 0,
            0, Constants.SCREEN_SIZE.height,
            Constants.SCREEN_SIZE.width - 50, Constants.SCREEN_SIZE.height - 50
    };

    public void start() {
        timeStarted = System.currentTimeMillis();
        Main.startTimeStepper(Constants.DT, this);
    }

    public void createCapabilitiesAndOpen() {
        window.setupContext();
        window.setVSync(vSync);
        bindEvents();

        GL.createCapabilities();  // do before anything gl related
        glClearColor(.0f, .0f, .0f, .0f);

        // for pixel perfect coordinates of vertices
        glOrtho(0, Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height, 0, -1, 1);

        window.show();
        setupShaders();
        setupPointers();
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
        glfwSetWindowPosCallback(window.handle, (window, xpos, ypos) -> {});
    }

    /** Must be called after window is visible */
    public void setupShaders() {
        // https://learnopengl.com/Getting-started/Shaders
        program = GL45.glCreateProgram();
        File shaderFolder = new File(Constants.SHADERS_FOLDER);
        ShaderHelper.searchDirectory(shaderFolder, program);
        GL45.glUseProgram(program);  // hook the program into our current context

        // place uniform values
        int resolutionLocation = GL45.glGetUniformLocation(program, "resolution");
        uniformTimeInx = GL45.glGetUniformLocation(program, "time");

        GL45.glUniform2f(resolutionLocation, Constants.SCREEN_SIZE.width, Constants.SCREEN_SIZE.height);
    }

    public void setupPointers() {
        // https://www.khronos.org/opengl/wiki/Vertex_Specification#Vertex_Buffer_Object
        // copy vertices into buffer
        vertBuff1 = GL45.glGenBuffers();
        GL45.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertBuff1);
        GL45.glBufferData(GL15.GL_ARRAY_BUFFER, verts, GL15.GL_STATIC_DRAW);

        // define the format of the buffer
        GL45.glEnableVertexAttribArray(0);  // only need one as we only have vertex position
        GL45.glVertexAttribPointer(0, 2, GL_FLOAT, false, Float.BYTES * 2, 0);
    }

    public void updateFps() {
        // update every second
        int newSeconds = (int) Math.floor(MathUtils.millisToSecond(System.currentTimeMillis()) - MathUtils.millisToSecond(timeStarted));
        if (newSeconds != secondsElapsed) {
            fps = frameCounter;
            frameCounter = 0;
            secondsElapsed = newSeconds;
        }
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        // update shader uniforms
        GL45.glUniform1f(uniformTimeInx, (float) glfwGetTime());

        // render here
        // https://docs.gl/gl2/glDrawArrays
        // https://www.songho.ca/opengl/gl_vertexarray.html

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

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
